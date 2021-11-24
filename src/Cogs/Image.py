from discord import File
from discord.ext import commands

from src.Utils import delete_temp_file, get_search_url


class Image(commands.Cog):
    @commands.command(description="Searches for image in Yandex and sends search URL")
    async def image(self, ctx):
        if (len(ctx.message.attachments) > 0):
            await ctx.message.attachments[-1].save(f'temp_data/{ctx.guild.id}/temp_image.jpg')
            await ctx.send('Search result\n' + get_search_url(f'temp_data/{ctx.guild.id}/temp_image.jpg'),
                           file=File(f'temp_data/{ctx.guild.id}/temp_image.jpg'))
            delete_temp_file(f'temp_data/{ctx.guild.id}/temp_image.jpg')
