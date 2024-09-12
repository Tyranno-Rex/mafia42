import 'package:flutter/material.dart';
import 'package:dio/dio.dart';
import 'package:mafia_client/waiting.dart';

class Game {
  final int id;
  final String name;
  final int playerCount;

  Game({required this.id, required this.name, required this.playerCount});

  factory Game.fromJson(Map<String, dynamic> json) {
    return Game(
      id: json['id'] as int,
      name: json['roomName'].toString(),
      playerCount: json['roomPlayerCount'] as int,
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

  @override
  void initState() {
    super.initState();
    _fetchGames();
  }

  Future<void> _fetchGames() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final response = await Dio().get('http://localhost:8080/game/all');

      if (response.statusCode == 200) {
        List<dynamic> gamesJson = response.data as List<dynamic>;

        setState(() {
          _games = gamesJson
              .map((game) => Game.fromJson(game as Map<String, dynamic>))
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
      final response = await Dio().post(
        'http://localhost:8080/game/create',
        data: {
          'gameName': _nameController.text,
          'gamePassword': _gamePasswordController.text,
          'gameStatus': 'WAITING',
          'gameOwner': widget.username,
          'gamePlayerCount': 1,
          'gameMaxPlayerCount': 8,
        },
      );
      _nameController.clear();
      _gamePasswordController.clear();
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
                        '플레이어 수: ${game.playerCount}',
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
