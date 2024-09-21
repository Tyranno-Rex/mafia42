import 'dart:async';
import 'dart:html';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:dio/dio.dart';
import 'package:mafia_client/lobby.dart';
import 'package:mafia_client/main.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';

class WaitingRoom extends StatelessWidget {
  final int gameId;
  final String username;

  WaitingRoom({required this.gameId, required this.username});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter WebSocket Chat',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: WaitingRoomPage(gameId: gameId, username: username),
    );
  }
}

class WaitingRoomPage extends StatefulWidget {
  final int gameId;
  final String username;

  WaitingRoomPage({required this.gameId, required this.username});

  @override
  _WaitingRoomPageState createState() => _WaitingRoomPageState();
}

class _WaitingRoomPageState extends State<WaitingRoomPage> {
  late StompClient _stompClient;
  final String _serverUrl = 'http://localhost:8080';
  final String _serverSocketUrl = 'http://localhost:8080/socket';

  final String _sendEndpoint = '/app/message';
  final String _subscribeEndpoint = '/topic/message';

  final String _checkConnectionEndpoint = '/app/check';

  final String _gameEndpoint = '/app/game';
  final String _gameSubscribeEndpoint = '/topic/game';

  late final String _userEndpoint = '/app/user';
  late final String _userSubscribeEndpoint = '/topic/user';

  final TextEditingController _messageController = TextEditingController();
  final List<Map<String, dynamic>> _messages = [];
  List<String> _users = [];
  List<String> _readyUsers = [];
  List<String> _aliveUsers = [];

  var _phaseStep = '';
  var _phaseTime = 0;
  var _phaseTimeMax = 0;

  Timer? _timer;
  var _isReady = false;
  String _gameRoomStatus = 'WAITING';
  var _MyRole = '';
  var _isAlive = true;
  bool _showNotification = false;
  String _notificationMessage = '';

  @override
  void initState() {
    var accessToken = window.localStorage['access'];

    super.initState();
    if (accessToken == null) {
      Navigator.push(
        context,
        MaterialPageRoute(
            builder: (context) => const MyHomePage(title: 'Mafia42')),
      );
    }
    _initializeWebSocket();
    _gameJoin();
  }

  void _initializeWebSocket() {
    _stompClient = StompClient(
      config: StompConfig.sockJS(
        url: _serverSocketUrl,
        onConnect: _onConnect,
        onWebSocketError: (dynamic error) => print('WebSocket Error: $error'),
        onStompError: (dynamic frame) => print('Stomp Error: $frame'),
        onDisconnect: (dynamic frame) => print('Disconnected: $frame'),
      ),
    );
    _stompClient.activate();
  }

  void _checkConnectionMessage() {
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      final message = {
        'content': 'connected',
        'username': widget.username,
        'gameId': widget.gameId.toInt(),
      };
      if (_stompClient.connected) {
        _stompClient.send(
          destination: _checkConnectionEndpoint,
          body: json.encode(message),
        );
      } else {
        print('Stomp client is not connected');
      }
    });
  }

  void _gameJoin() async {
    var accessToken = window.localStorage['access'];
    final joinData = {
      'gameId': widget.gameId,
      'userName': widget.username,
    };
    final response = await Dio().post(
      '$_serverUrl/game/join',
      data: joinData,
      options: Options(
        contentType: 'application/json',
        headers: {
          'access': accessToken,
        },
      ),
    );
    if (response.statusCode == 200) {
      print('Joined room ${widget.gameId}');
      _checkConnectionMessage();
    } else {
      print('Failed to join room ${widget.gameId}');
    }
  }

  void _gameLeave() async {
    var accessToken = window.localStorage['access'];
    final leaveData = {
      'gameId': widget.gameId,
      'userName': widget.username,
    };
    final response = await Dio().post(
      '$_serverUrl/game/leave',
      data: leaveData,
      options: Options(
        contentType: 'application/json',
        headers: {
          'access': accessToken,
        },
      ),
    );
    if (response.statusCode == 200) {
      print('Left room ${widget.gameId}');
    } else {
      print('Failed to leave room ${widget.gameId}');
    }
  }

  void _gameReady() async {
    var accessToken = window.localStorage['access'];
    final readyData = {
      'gameId': widget.gameId,
      'username': widget.username,
      'readyStatus': !_isReady,
    };
    final response = await Dio().post(
      '$_serverUrl/game/ready',
      data: readyData,
      options: Options(
        contentType: 'application/json',
        headers: {
          'access': accessToken,
        },
      ),
    );
    // response.put("status", "success");
    if (response.statusCode == 200) {
      print('Ready room ${widget.gameId}');
      if (_isReady == false) {
        _readyUsers.add(widget.username);
        _isReady = true;
      } else {
        _readyUsers.remove(widget.username);
        _isReady = false;
      }
    } else {
      print('Failed to ready room ${widget.gameId}');
    }
  }

  void _sendMessage() {
    if (_messageController.text.isNotEmpty) {
      final message = {
        'content': _messageController.text,
        'uuid': widget.username,
        'gameId': widget.gameId
      };
      _stompClient.send(
        destination: _sendEndpoint,
        body: json.encode(message),
      );
      _messageController.clear();
    }
  }

  void _onConnect(StompFrame frame) {
    _stompClient.subscribe(
      destination: '$_subscribeEndpoint/${widget.gameId}',
      callback: (StompFrame frame) {
        if (frame.body != null) {
          final message = json.decode(frame.body!);
          setState(() {
            _messages.add(message);
          });
        }
      },
    );

    _stompClient.subscribe(
      destination: '$_gameSubscribeEndpoint/${widget.gameId}',
      callback: (StompFrame frame) {
        if (frame.body != null) {
          final game = json.decode(frame.body!);
          _processGameUpdate(game);
        }
      },
    );

    _stompClient.subscribe(
      destination:
          '$_userSubscribeEndpoint/${widget.gameId}/${widget.username}',
      callback: (StompFrame frame) {
        if (frame.body != null) {
          final message = json.decode(frame.body!);
          setState(() {
            _MyRole = message['role'];
            _isAlive = message['alive'];
          });

          if (message['message'] != null && message['message'] != "ROLE") {
            _GameNotifyDialog(context, message['message']);
          } else {
            _showRoleDialog(context, message['role']);
          }
        }
      },
    );
  }

  void _processGameUpdate(Map<String, dynamic> game) {
    var userList = game['players'].map((player) => player['userName']).toList();
    var playerReady = game['players']
        .where((player) => player['isReady'] == true)
        .map((player) => player['userName'])
        .toList();
    var playerAlive = game['players']
        .where((player) => player['isAlive'] == true)
        .map((player) => player['userName'])
        .toList();

    print('userList: $userList');
    print('player Alive: $playerAlive');

    _phaseStep = game['phaseStep'] ?? '';
    _phaseTime = game['phaseTime'] ?? 0;
    _phaseTimeMax = game['phaseTimeMax'] ?? 0;

    if (game['playerDoctorSaved'] != null &&
        game['playerDoctorSaved'].isNotEmpty &&
        game['playerMafiaKill'] != null &&
        game['playerMafiaKill'].isNotEmpty) {
      if (game['playerDoctorSaved'] == game['playerMafiaKill']) {
        _showNotification = true;
        _notificationMessage = '의사가 ${game['playerMafiaKill']}를 구했습니다.';
      } else {
        _showNotification = true;
        _notificationMessage = '한밤 중 ${game['playerMafiaKill']}가 살해되었습니다.';
      }
      game['playerDoctorSaved'] = '';
      game['playerMafiaKill'] = '';
    }

    if (game['message'] != null && game['message'] != "") {
      _showNotification = true;
      _notificationMessage = game['message'];
    }

    setState(() {
      _users = userList.cast<String>();
      _readyUsers = playerReady.cast<String>();
      _aliveUsers = playerAlive.cast<String>();
      if (_showNotification) {
        _GameNotifyDialog(context, _notificationMessage);
        _showNotification = false;
      }
    });
  }

  void _showRoleDialog(BuildContext context, String role) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('새로운 역할 정보'),
          content: Text('당신의 역할은 $role 입니다.'),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
              },
              child: const Text('확인'),
            ),
          ],
        );
      },
    );
  }

  void _GameNotifyDialog(BuildContext context, String message) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('게임 알림'),
          content: Text(message),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
              },
              child: const Text('확인'),
            ),
          ],
        );
      },
    );
  }

  @override
  void dispose() {
    _stompClient.deactivate();
    _timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Chat Room ${widget.gameId}'),
      ),
      body: Column(
        children: [
          Column(
            children: [
              Text('현재 단계: $_phaseStep'),
              Text('남은 시간: $_phaseTime / $_phaseTimeMax'),
              Text('내 역할: $_MyRole'),
            ],
          ),
          ButtonBar(
            alignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: () {
                  _gameLeave();
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                        builder: (context) =>
                            GameLobby(username: widget.username)),
                  );
                },
                child: Text('나가기'),
              ),
              ElevatedButton(
                onPressed: () {
                  _gameReady();
                },
                child: Text('준비'),
              ),
            ],
          ),
          _buildUserList(),
          Expanded(
            child: ListView.builder(
              itemCount: _messages.length,
              itemBuilder: (context, index) {
                final message = _messages[index];
                final isCurrentUser = message['uuid'] == widget.username;
                return _buildMessageBubble(message, isCurrentUser);
              },
            ),
          ),
          _buildMessageInput(),
        ],
      ),
    );
  }

  Widget _buildUserList() {
    return Container(
      height: 60,
      padding: EdgeInsets.symmetric(vertical: 8),
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        itemCount: _users.length,
        itemBuilder: (context, index) {
          return Container(
            margin: EdgeInsets.symmetric(horizontal: 4),
            padding: EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
              color: _phaseStep == ''
                  ? (_readyUsers.contains(_users[index])
                      ? Colors.green[100]
                      : Colors.grey[300])
                  : (_aliveUsers.contains(_users[index])
                      ? Colors.green[100]
                      : Colors.red[300]),
              borderRadius: BorderRadius.circular(20),
            ),
            child: Center(
              child: Text(
                _users[index],
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildMessageBubble(Map<String, dynamic> message, bool isCurrentUser) {
    return Align(
      alignment: isCurrentUser ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: EdgeInsets.symmetric(vertical: 5, horizontal: 10),
        padding: EdgeInsets.symmetric(vertical: 10, horizontal: 15),
        decoration: BoxDecoration(
          color: isCurrentUser ? Colors.blue[100] : Colors.grey[300],
          borderRadius: BorderRadius.circular(20),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              message['uuid'],
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12),
            ),
            SizedBox(height: 5),
            Text(message['content']),
          ],
        ),
      ),
    );
  }

  Widget _buildMessageInput() {
    return Container(
      padding: EdgeInsets.all(8),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: _messageController,
              decoration: InputDecoration(
                hintText: 'Type a message...',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(25),
                ),
                contentPadding:
                    EdgeInsets.symmetric(horizontal: 20, vertical: 10),
              ),
            ),
          ),
          SizedBox(width: 8),
          FloatingActionButton(
            onPressed: _sendMessage,
            child: Icon(Icons.send),
            mini: true,
          ),
        ],
      ),
    );
  }
}
