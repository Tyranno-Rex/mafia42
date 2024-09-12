import 'package:flutter/material.dart';
import 'package:mafia_client/lobby.dart';
import 'package:dio/dio.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Mafia42',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
            seedColor: const Color.fromARGB(255, 209, 7, 0)),
        useMaterial3: true,
      ),
      home: const MyHomePage(
          title: 'Mafia42: Jeong Eun Seong simple implementation'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});
  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  // login
  void _login() async {
    final loginData = {
      'username': _nameController.text,
      'password': _passwordController.text,
    };

    final response = await Dio().post('http://localhost:8080/login',
        data: loginData,
        options: Options(
          headers: {'Content-Type': 'application/json'},
        ));
  }

  // sign up
  void _signUp() async {
    final signUpData = {
      'username': _nameController.text,
      'password': _passwordController.text,
    };

    final response = await Dio().post('http://localhost:8080/signup',
        data: signUpData,
        options: Options(
          headers: {'Content-Type': 'application/json'},
        ));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'Enter Your Name',
            ),
            TextField(
              controller: _nameController,
              decoration: const InputDecoration(
                border: OutlineInputBorder(),
                labelText: 'Name',
              ),
            ),
            TextField(
              controller: _passwordController,
              decoration: const InputDecoration(
                border: OutlineInputBorder(),
                labelText: 'Password',
              ),
            ),
            // Loign button
            ElevatedButton(
              onPressed: () {
                _login();
              },
              child: const Text('Login'),
            ),
            // Sign up button
            ElevatedButton(
              onPressed: () {
                _signUp();
              },
              child: const Text('Sign Up'),
            ),

            // ElevatedButton(
            //   onPressed: () {
            //     Navigator.push(
            //       context,
            //       MaterialPageRoute(
            //           builder: (context) =>
            //               GameLobby(username: _nameController.text)),
            //     );
            //   },
            //   child: const Text('Enter Room'),
            // ),
          ],
        ),
      ),
    );
  }
}
