import 'dart:async';
import 'dart:html';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:dio/dio.dart';
import 'package:mafia_client/main.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

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

  final TextEditingController _messageController = TextEditingController();
  final List<Map<String, dynamic>> _messages = [];
  final List<String> _users = [];
  Timer? _timer;
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
    _checkConnectionMessage();
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
    print("accessToken: $accessToken");
    final joinData = {
      'gameId': widget.gameId,
      'userName': widget.username,
    };
    final response = await Dio().post(
      '$_serverUrl/game/join',
      data: joinData,
      options: Options(
        headers: {
          'access': accessToken,
        },
      ),
    );
    if (response.statusCode == 200) {
      print('Joined room ${widget.gameId}');
    } else {
      print('Failed to join room ${widget.gameId}');
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
          final userList =
              game['players'].map((player) => player['userName']).toList();
          setState(() {
            _users.clear();
            _users.addAll(userList.cast<String>());
          });
        }
      },
    );}

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
              color: Colors.blue[100],
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
