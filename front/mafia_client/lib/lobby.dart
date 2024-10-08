import 'dart:async';

import 'package:flutter/material.dart';
import 'package:dio/dio.dart';
import 'package:mafia_client/main.dart';
import 'package:mafia_client/chatting.dart';
import 'dart:html';

class Game {
  final int id;
  final String name;
  final int playerCount;
  final int maxplayerCount;

  Game(
      {required this.id,
      required this.name,
      required this.playerCount,
      required this.maxplayerCount});

  factory Game.fromJson(Map<String, dynamic> json) {
    return Game(
      id: json['id'] as int,
      name: json['gameName'].toString(),
      playerCount: json['gamePlayerCount'] as int,
      maxplayerCount: json['gameMaxPlayerCount'] as int,
    );
  }
}

class GameLobby extends StatefulWidget {
  final String username;

  GameLobby({required this.username});

  @override
  _GameRoomsListState createState() => _GameRoomsListState();
}

class _GameRoomsListState extends State<GameLobby> {
  List<Game> _games = [];
  bool _isLoading = true;
  final _nameController = TextEditingController();
  final _gamePasswordController = TextEditingController();
  final _gameMaxPlayerCountController = TextEditingController();

  @override
  void initState() {
    var accessToken = window.localStorage['access'];
    super.initState();
    _fetchGames();
    if (accessToken == null) {
      Navigator.push(
        context,
        MaterialPageRoute(
            builder: (context) => const MyHomePage(title: 'Mafia42')),
      );
    }
  }

  Future<void> _fetchGames() async {
    setState(() {
      _isLoading = true;
    });

    try {
      var accessToken = window.localStorage['access'];
      print('accessToken: $accessToken');
      final response = await Dio().get(
        'http://localhost:8080/game/all',
        options: Options(
          contentType: Headers.jsonContentType,
          headers: {
            'access': accessToken,
            'Content-Type': 'application/json',
          },
        ),
      );
      print('Response data: ${response.data}');
      if (response.statusCode == 200) {
        Map<String, dynamic> gamesJson = response.data as Map<String, dynamic>;
        print(gamesJson);

        setState(() {
          _games = gamesJson.entries
              .map((game) => Game.fromJson(game.value as Map<String, dynamic>))
              .toList();
          _isLoading = false;
        });
      } else {
        throw Exception('Failed to load games');
      }
    } catch (e) {
      print('Error fetching games: $e');
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _createGame() async {
    try {
      var accessToken = window.localStorage['access'];
      print('accessToken: $accessToken');
      final response = await Dio().post(
        'http://localhost:8080/game/create',
        data: {
          'gameName': _nameController.text,
          'gamePassword': _gamePasswordController.text,
          'gameStatus': 'CREATED',
          'gameOwner': widget.username,
          'gamePlayerCount': 0,
          'gameMaxPlayerCount': int.parse(_gameMaxPlayerCountController.text),
        },
        options: Options(
          contentType: 'application/json',
          headers: {
            'access': accessToken,
          },
        ),
      );
      print('Response data: ${response.data}');
      _nameController.clear();
      _gamePasswordController.clear();
      _gameMaxPlayerCountController.clear();
      if (response.statusCode == 200) {
        response.data['status'] == 'success'
            ? _fetchGames()
            : throw Exception('Failed to create game');
      } else {
        throw Exception('Failed to create game');
      }
    } catch (e) {
      print('Error creating game: $e');
    }
  }

  void _showCreateGameDialog() {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('게임 방 추가'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: _nameController,
                decoration: const InputDecoration(
                  labelText: '방 이름',
                  hintText: '방 이름을 입력하세요',
                ),
              ),
              TextField(
                controller: _gamePasswordController,
                decoration: const InputDecoration(
                  labelText: '비밀번호',
                  hintText: '비밀번호를 입력하세요',
                ),
              ),
              TextField(
                controller: _gameMaxPlayerCountController,
                decoration: const InputDecoration(
                  labelText: '최대 플레이어 수',
                  hintText: '최대 플레이어 수를 입력하세요',
                ),
              )
            ],
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.pop(context);
              },
              child: Text('취소'),
            ),
            TextButton(
              onPressed: () {
                _createGame();
                Navigator.pop(context);
              },
              child: Text('확인'),
            ),
          ],
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('게임 방 목록'),
        backgroundColor: Colors.deepPurple,
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _fetchGames,
              child: ListView.builder(
                itemCount: _games.length,
                itemBuilder: (context, index) {
                  final game = _games[index];
                  return Card(
                    margin:
                        const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    elevation: 4,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: ListTile(
                      contentPadding: const EdgeInsets.all(16),
                      leading: CircleAvatar(
                        backgroundColor: Colors.deepPurpleAccent,
                        child: Text(
                          game.name.substring(0, 1).toUpperCase(),
                          style: const TextStyle(color: Colors.white),
                        ),
                      ),
                      title: Text(
                        game.name,
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 18,
                        ),
                      ),
                      subtitle: Text(
                        '플레이어 수/최대 수용인원: ${game.playerCount}/${game.maxplayerCount}',
                        style: const TextStyle(fontSize: 14),
                      ),
                      trailing: const Icon(Icons.arrow_forward_ios),
                      onTap: () {
                        // 게임 방 선택 시 동작 구현
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (context) => WaitingRoom(
                                  gameId: game.id, username: widget.username)),
                        );
                        print('Selected game: ${game.id}');
                        print('Selected game: ${game.name}');
                      },
                    ),
                  );
                },
              ),
            ),

      // 방 추가와 방 목록 새로고침을 위한 플로팅 액션 버튼
      floatingActionButton: Stack(
        children: [
          Positioned(
            bottom: 80,
            right: 16,
            child: FloatingActionButton(
              heroTag: 'createGame',
              onPressed: _showCreateGameDialog,
              backgroundColor: Colors.deepPurple,
              child: const Icon(Icons.add),
            ),
          ),
          Positioned(
            bottom: 16,
            right: 16,
            child: FloatingActionButton(
              heroTag: 'refreshGames',
              onPressed: _fetchGames,
              backgroundColor: Colors.deepPurple,
              child: const Icon(Icons.refresh),
            ),
          ),
        ],
      ),
    );
  }
}
