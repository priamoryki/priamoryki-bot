import asyncio
from random import shuffle
from re import match
from sys import platform

from discord import player
from discord.ext import commands
from pytube import YouTube, Playlist, Search
from pytube.exceptions import PytubeError

from src.Utils import Data, Song, ServerInfo, get_spotify, delete_temp_file


class Player(commands.Cog):
    @commands.command(description="Joins users voice_channel if audio isn't currently playing")
    async def join(self, ctx):
        if (ctx.author.voice is None or ctx.author.voice.channel is None):
            return
        elif (ctx.voice_client is None):
            await ctx.author.voice.channel.connect()
        elif (not Data.servers[ctx.guild.id].is_playing_audio()):
            await ctx.voice_client.move_to(ctx.author.voice.channel)
        asyncio.create_task(self._disconnector(ctx))

    @commands.command(description="Leaves voice_channel if it's connected")
    async def leave(self, ctx):
        if (ctx.voice_client is not None):
            await ctx.voice_client.disconnect()

    async def resume(self, ctx):
        ctx.voice_client.resume()

    async def pause(self, ctx):
        ctx.voice_client.pause()

    async def stop(self, ctx):
        ctx.voice_client.stop()

    async def _disconnector(self, ctx):
        while (True):
            await asyncio.sleep(300)
            if (not Data.servers[ctx.guild.id].is_playing_audio()):
                break
        await self.leave(ctx)

    @commands.command(description="Skips current audio if it's playing")
    async def skip(self, ctx):
        guild_id = ctx.guild.id
        server = Data.servers[guild_id]
        if (server.is_playing_audio()):
            old_on_repeat = server.on_repeat
            server.on_repeat = False
            await self.stop(ctx)
            await server.play_next_audio.wait()
            server.on_repeat = old_on_repeat

    async def audio_player(self, ctx):
        guild_id = ctx.guild.id
        server = Data.servers[guild_id]
        while (server.q):
            server.play_next_audio.clear()
            server.current_song = server.q.popleft()
            if (ctx.voice_client is not None):
                ffmpeg_file = 'ffmpeg/bin/ffmpeg.exe' if platform.startswith('win') else 'ffmpeg'
                ctx.voice_client.play(player.FFmpegPCMAudio(executable=ffmpeg_file,
                                                            source=str(server.current_song)),
                                      after=lambda _: server.play_next_audio.set())
                await server.play_next_audio.wait()
            if (ctx.voice_client is not None and server.on_repeat):
                server.q.appendleft(server.current_song)
            else:
                delete_temp_file(str(server.current_song))
            await asyncio.sleep(0.200)
        Data.servers[guild_id] = ServerInfo()

    async def play(self, ctx, path: str, song_name: str = "default music"):
        guild_id = ctx.guild.id
        server = Data.servers[guild_id]
        await self.join(ctx)
        if (ctx.voice_client is not None):
            server.q.append(Song(song_name, path, ctx.author.name))
            if (not server.is_playing_audio()):
                server.task = asyncio.create_task(self.audio_player(ctx))

    async def play_song_from_youtube(self, ctx, video_url: str):
        guild_id = ctx.guild.id
        server = Data.servers[guild_id]
        output_path, filename = f'temp_data/{guild_id}/', f'temp_song{server.songs_counter}.mp3'
        """This is temporary may be out of work cause of YT API update"""
        video = YouTube(video_url).streams.get_audio_only()
        video.download(output_path=output_path,
                       filename=filename)
        server.songs_counter += 1
        print(f'{server.songs_counter}) {video_url}')
        await self.play(ctx, output_path + filename, YouTube(video_url).title)

    async def play_playlist_from_youtube(self, ctx, playlist_url: str):
        for url in Playlist(playlist_url).video_urls:
            try:
                await self.play_song_from_youtube(ctx, url)
            except PytubeError:
                pass
            await asyncio.sleep(1)

    async def play_song_from_spotify(self, ctx, song_url: str):
        # temporary crutch for spotify
        track = get_spotify().track(song_url)
        query = f"{track['artists'][0]['name']} - {track['name']}"
        await self.play_song_from_youtube(ctx, Search(query).results[0].watch_url)

    async def play_album_from_spotify(self, ctx, album_url: str):
        for song in get_spotify().album(album_url)['tracks']['items']:
            try:
                await self.play_song_from_spotify(ctx, song['external_urls']['spotify'])
            except Exception:
                pass
            await asyncio.sleep(1)

    @commands.command(description="Sets repeat")
    async def repeat(self, ctx, arg: str):
        guild_id = ctx.guild.id
        server = Data.servers[guild_id]
        if (arg.upper() == 'ON'):
            server.on_repeat = True
        elif (arg.upper() == 'OFF'):
            server.on_repeat = False

    @commands.command(description="Clears the queue")
    async def clear_queue(self, ctx):
        while (Data.servers[ctx.guild.id].q):
            delete_temp_file(str(Data.servers[ctx.guild.id].q.popleft()))
        await self.skip(ctx)

    @commands.command(description="Shuffles the queue")
    async def shuffle_queue(self, ctx):
        shuffle(Data.servers[ctx.guild.id].q)

    @commands.command(description="Prints the queue")
    async def print_queue(self, ctx):
        server = Data.servers[ctx.guild.id]
        if (server.is_playing_audio()):
            counter, q = 1, server.q.copy()
            result = f'Playing now: `{server.current_song.name}` *by* ***{server.current_song.client_username}***\n'
            if (q):
                result += f'__Queue__:\n'
            while (q):
                elem = q.popleft()
                result += f'{counter}) `{elem.name}` *by* ***{elem.client_username}***\n'
                counter += 1
            await ctx.send(result)

    @commands.command(description="Parses music video or playlist from YouTube or Spotify and adds it to the queue",
                      aliases=["Music", "музыка", "Музыка"])
    async def music(self, ctx, *args: str):
        possible_links = {
            r'^(https?://)?(www\.)?youtube\.com/(watch\?v=|embed/|v/|.+\?v=)?[^&=%\?]{11}': self.play_song_from_youtube,
            r'^(https?://)?(www\.)?youtube\.com/playlist\?list=[^#\&\?]{34}$': self.play_playlist_from_youtube,
            r'^(https?://)?(www\.)?open\.spotify\.com/track/[^&=%\?]{22}$': self.play_song_from_spotify,
            r'^(https?://)?(www\.)?open\.spotify\.com/album/[^&=%\?]{22}$': self.play_album_from_spotify
        }
        if (len(args) == 1):
            for regex in possible_links:
                if (match(regex, args[-1])):
                    await possible_links[regex](ctx, args[-1])
                    return
        await self.play_song_from_youtube(ctx, Search(' '.join(args)).results[0].watch_url)
