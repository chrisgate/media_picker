import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:media_picker/media_picker.dart';
import 'dart:io';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  File _pickerFile;
  File _takePicFile;
  List<File> _pickerFiles;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await MediaPicker.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('$_platformVersion'),
        ),
        body: ListView(
          children: <Widget>[
            buildRaisedButton(),
            buildImage(_pickerFile),
            RaisedButton(
              onPressed: () {
                MediaPicker.takePhoto(type: CameraType.IDCardFront)
                    .then((file) {
                  setState(() {
                    _takePicFile = file;
                  });
                }).catchError((error) {
                  debugPrint("error:$error");
                });
              },
              child: Text("拍照"),
            ),
            buildImage(_takePicFile),
            RaisedButton(
              onPressed: () {
                MediaPicker.pickMedias(maxSelectCount: 9).then((files) {
                  setState(() {
                    _pickerFiles = files;
                  });
                }).catchError((error) {
                  debugPrint("error:$error");
                });
              },
              child: Text("多选图片或视频"),
            ),
          ],
        ),
      ),
    );
  }

  RaisedButton buildRaisedButton() => RaisedButton(
        onPressed: () {
          MediaPicker.pickMedia().then((file) {
            setState(() {
              _pickerFile = file;
            });
          }).catchError((error) {
            debugPrint("error:$error");
          });
        },
        child: Text("单选图片或视频"),
      );

  Widget buildImage(File file) {
    return file != null
        ? Image.file(file)
        : Image.network(
            "https://avatars2.githubusercontent.com/u/20411648?s=460&v=4");
  }
}
