__author__ = "Pavel Lymar"
__copyright__ = "None"
__credits__ = []
__license__ = "None"
__version__ = "0.5"
__maintainer__ = "Pavel Lymar"
__email__ = "None"
__status__ = "Production"

import json

from discord import Intents, PermissionOverwrite, DiscordException
from discord.ext import commands

from src.Cogs import Audio, Image, Sounds
from src.Utils import Data, ServerInfo, parse_readme, get_yadisk

intents = Intents.all()
bot = commands.Bot(command_prefix=Data.settings['prefix'], intents=intents)
Data.audio_cog = Audio.Player()
bot.add_cog(Data.audio_cog)
bot.add_cog(Sounds.Sounds())
bot.add_cog(Image.Image())


async def set_message_reactions(msg):
    await msg.clear_reactions()
    for reaction in REACTIONS:
        await msg.add_reaction(reaction)


async def edit_all_main_messages(text: str):
    for i in Data.settings['servers']:
        channel_id = Data.get_main_channel_id(i)
        message_id = Data.get_main_message_id(i)
        msg = await bot.get_guild(int(i)).get_channel(channel_id).fetch_message(message_id)
        await msg.edit(content=text)
        await set_message_reactions(msg)


@bot.event
async def on_guild_join(guild):
    channel = guild.get_channel(Data.get_main_channel_id(guild.id))
    if (channel is None):
        overwrites = {
            guild.default_role: PermissionOverwrite(read_messages=False)
        }
        channel = await guild.create_text_channel(Data.settings['bot'], overwrites=overwrites)
        Data.settings['servers'][str(guild.id)]['channel_id'] = channel.id

    msg = None
    try:
        msg = await channel.fetch_message(Data.get_main_message_id(guild.id))
    except (DiscordException):
        pass
    if (msg is None):
        msg = await channel.send(f'```{parse_readme()}```')
        Data.settings['servers'][str(guild.id)]['message_id'] = msg.id
        await msg.pin()
        await set_message_reactions(msg)
        await clear_all(await bot.get_context(msg))
        Data.servers[guild.id] = ServerInfo()
        json.dump(Data.settings, open('src/config.json', 'w'), ensure_ascii=False, indent=2)
        get_yadisk().upload("src/config.json", "config.json", overwrite=True)


@bot.event
async def on_guild_remove(guild):
    Data.servers[guild.id] = ServerInfo()


@bot.event
async def on_ready():
    print('Bot is working now!')


async def clear(ctx):
    await ctx.channel.purge(limit=1, check=lambda msg: msg.author.id != Data.settings['id'])


async def clear_all(ctx):
    await ctx.channel.purge(limit=None, check=lambda msg: msg.id != Data.get_main_message_id(ctx.guild.id))


@bot.event
async def on_raw_reaction_add(payload):
    channel = bot.get_channel(payload.channel_id)
    msg = await channel.fetch_message(payload.message_id)
    if (msg.id == Data.get_main_message_id(payload.guild_id) and payload.member.id != Data.settings['id']):
        try:
            await REACTIONS[payload.emoji.name](await bot.get_context(msg))
        except (KeyError, Exception):
            pass
        finally:
            await msg.remove_reaction(payload.emoji, payload.member)


@bot.event
async def on_message(ctx):
    if (ctx.channel.id == Data.get_main_channel_id(ctx.guild.id)):
        await clear(ctx)
        await bot.process_commands(ctx)


REACTIONS = {
    '▶️': Data.audio_cog.resume,
    '⏸️': Data.audio_cog.pause,
    '⏯️': Data.audio_cog.skip,
    '🔁': lambda ctx: Data.audio_cog.repeat(ctx, 'ON'),
    '🧹': clear_all
}

bot.run(Data.settings['token'])
