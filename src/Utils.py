import json
from asyncio import Event, Task
from collections import deque
from dataclasses import dataclass
from os import remove

import requests
from spotipy import Spotify, SpotifyClientCredentials
from yadisk import YaDisk

YADISK_TOKEN = "???"
SPOTIFY_CLIENT_ID = "???"
SPOTIFY_CLIENT_SECRET = "???"


@dataclass(frozen=True)
class Song:
    name: str
    path: str
    client_username: str

    def __str__(self):
        return self.path


SONG_TYPE = Song


@dataclass()
class ServerInfo:
    q: deque[SONG_TYPE] = deque()
    play_next_audio: Event = Event()
    task: Task = None
    current_song: SONG_TYPE = None
    on_repeat: bool = False
    songs_counter: int = 0

    def is_playing_audio(self) -> bool:
        return self.task is not None


def parse_config():
    get_yadisk().download('config.json', 'src/config.json')
    return json.load(open('src/config.json'))


def delete_temp_file(path: str):
    if (path.startswith('temp_data/')):
        try:
            remove(path)
        except FileNotFoundError:
            pass


def parse_readme() -> str:
    with open('README.md') as file:
        lines = [s.strip('*').strip() for s in file.readlines()]
        return lines[2] + '\n' + '\n'.join(lines[3:])


def get_yadisk() -> YaDisk:
    return YaDisk(token=YADISK_TOKEN)


def get_spotify() -> Spotify:
    return Spotify(client_credentials_manager=SpotifyClientCredentials(client_id=SPOTIFY_CLIENT_ID,
                                                                       client_secret=SPOTIFY_CLIENT_SECRET))


def get_search_url(file_path: str) -> str:
    search_url = 'https://yandex.ru/images/search'
    files = {'upfile': ('blob', open(file_path, 'rb'), 'image/jpeg')}
    params = {
        'rpt': 'imageview',
        'format': 'json',
        'request': '{"blocks":[{"block":"b-page_type_search-by-image__link"}]}'
    }
    query_string = json.loads(
        requests.post(search_url, params=params, files=files).content
    )['blocks'][0]['params']['url']
    return f'{search_url}?{query_string}&cbir_page=similar'


class Data:
    audio_cog = None
    settings = parse_config()
    servers: dict[int, ServerInfo] = {int(server): ServerInfo() for server in settings['servers']}

    @staticmethod
    def get_main_channel_id(guild_id):
        if (Data.settings['servers'].get(str(guild_id)) is None):
            return None
        return Data.settings['servers'][str(guild_id)]['channel_id']

    @staticmethod
    def get_main_message_id(guild_id):
        if (Data.settings['servers'].get(str(guild_id)) is None):
            return None
        return Data.settings['servers'][str(guild_id)]['message_id']


'''
    @staticmethod
    def get_server(ind: int):
        return Data.servers[ind]

    @staticmethod
    def get_play_next_audio(ind: int):
        return Data.get_server(ind).play_next_audio

    @staticmethod
    def get_queue(ind: int):
        return Data.get_server(ind).q

    @staticmethod
    def get_task(ind: int):
        return Data.get_server(ind).task

    @staticmethod
    def get_current_song(ind: int):
        return Data.get_server(ind).current_song

    @staticmethod
    def get_on_repeat(ind: int):
        return Data.get_server(ind).on_repeat

    @staticmethod
    def get_songs_counter(ind: int):
        return Data.get_server(ind).songs_counter

    @staticmethod
    def add_song(ind: int, song: Song, left: bool = False):
        Data.get_queue(ind).appendleft(song) if left else Data.get_queue(ind).append(song)
'''
