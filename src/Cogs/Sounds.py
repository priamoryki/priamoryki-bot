from discord.ext import commands

from src.Utils import Data


class Sounds(commands.Cog):
    @commands.command()
    async def kaguya(self, ctx):
        await Data.audio_cog.play(ctx, 'sounds/kaguya.mp3')

    @commands.command()
    async def running(self, ctx):
        await Data.audio_cog.play(ctx, 'sounds/running.mp3')

    @commands.command()
    async def boobs(self, ctx):
        await Data.audio_cog.play(ctx, 'sounds/boobs.mp3')

    @commands.command()
    async def tuturu(self, ctx):
        await Data.audio_cog.play(ctx, 'sounds/tuturu.mp3')

    @commands.command()
    async def nya(self, ctx):
        await Data.audio_cog.play(ctx, 'sounds/nya.mp3')

    @commands.command()
    async def ohhh(self, ctx):
        await Data.audio_cog.play(ctx, 'sounds/ohhh.mp3')

    @commands.command()
    async def nikoni(self, ctx):
        await Data.audio_cog.play(ctx, 'sounds/nikoni.mp3')

    @commands.command()
    async def gj(self, ctx):
        await Data.audio_cog.play(ctx, 'sounds/gj.mp3')

    @commands.command()
    async def titan(self, ctx):
        await Data.audio_cog.play(ctx, 'sounds/titan.mp3')

    @commands.command()
    async def wtf(self, ctx):
        await Data.audio_cog.play(ctx, 'sounds/wtf.mp3')

    @commands.command()
    async def senpai(self, ctx):
        await Data.audio_cog.play(ctx, 'sounds/senpai.mp3')

    @commands.command()
    async def ohio(self, ctx):
        await Data.audio_cog.play(ctx, 'sounds/ohio.mp3')

    @commands.command()
    async def silence(self, ctx):
        await Data.audio_cog.play(ctx, 'sounds/silence.mp3')
