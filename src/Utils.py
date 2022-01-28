import json
from asyncio import Event, Task
from collections import deque
from dataclasses import dataclass
from datetime import timedelta
from typing import *
from os import remove

import sqlite3
import requests
from spotipy import Spotify, SpotifyClientCredentials
from yadisk import YaDisk


def get_normal_time(seconds: float) -> str:
    return str(timedelta(seconds=seconds))


@dataclass(frozen=True)
class Song:
    name: str
    path: str
    client_username: str
    duration: float

    def __str__(self) -> str:
        return self.path

    def print_data(self) -> str:
        return f'`{self.name}` *by* ***{self.client_username}***  [`{get_normal_time(self.duration)}`]'


SONG_TYPE = Song


@dataclass()
class ServerInfo:
    q: Deque[SONG_TYPE] = deque()
    play_next_audio: Event = Event()
    task: Optional[Task] = None
    current_song: Optional[SONG_TYPE] = None
    on_repeat: bool = False
    songs_counter: int = 0

    def is_playing_audio(self) -> bool:
        return self.task is not None


def parse_config() -> Dict:
    return json.load(open('data/config.json'))


def connect_to_servers_db():
    get_yadisk().download('servers.db', 'data/servers.db')
    return sqlite3.connect('data/servers.db', isolation_level=None)


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


SETTINGS = parse_config()


def get_yadisk() -> YaDisk:
    return YaDisk(token=SETTINGS['YADISK_TOKEN'])


def get_spotify() -> Spotify:
    return Spotify(client_credentials_manager=SpotifyClientCredentials(SETTINGS['SPOTIFY_CLIENT_ID'],
                                                                       SETTINGS['SPOTIFY_CLIENT_SECRET']))


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


SERVERS_DB_CONNECTION = connect_to_servers_db()


def get_all_servers_ids() -> List[int]:
    return list(map(lambda a: int(*a), SERVERS_DB_CONNECTION.execute(f"SELECT server_id from servers").fetchall()))


class Data:
    audio_cog = None
    settings: Dict = SETTINGS
    servers_db = SERVERS_DB_CONNECTION
    servers: Dict[int, ServerInfo] = {server: ServerInfo() for server in get_all_servers_ids()}

    @staticmethod
    def get_main_channel_id(guild_id: int) -> int:
        return int(*Data.servers_db.execute(f"SELECT channel_id FROM servers WHERE server_id = {guild_id}").fetchone())

    @staticmethod
    def get_main_message_id(guild_id: int) -> int:
        return int(*Data.servers_db.execute(f"SELECT message_id FROM servers WHERE server_id = {guild_id}").fetchone())
