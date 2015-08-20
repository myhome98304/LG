/*
 *  Copyright (c) 2014, Parse, LLC. All rights reserved.
 *
 *  You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 *  copy, modify, and distribute this software in source code or binary form for use
 *  in connection with the web services and APIs provided by Parse.
 *
 *  As with any software that integrates with the Parse platform, your use of
 *  this software is subject to the Parse Terms of Service
 *  [https://www.parse.com/about/terms]. This copyright notice shall be
 *  included in all copies or substantial portions of the software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.application.csproject6.smartalarmwalkietalkie;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SampleApplication extends Application {
    private ArrayList<ParseObject> groupList;

    public ArrayList<String> LazyUserList;
    public List<String> songs = new ArrayList<String>();
    int currentPosition;

    MediaPlayer music;
    private ParseObject current_group;
    @Override
    public void onCreate() {
        super.onCreate();
        currentPosition = 0;

    // Required - Initialize the Parse SDK
      Parse.enableLocalDatastore(this);

      Parse.initialize(this, "mjRo8FfBlkQj9ChdXFj363WDD96xnT5h7qQJ9BGJ", "UZUQNm6OfQvRmVkHffo5k3qGpNHhRT9i3oH52QUS");

      ParseInstallation.getCurrentInstallation().saveInBackground();
      ParsePush.subscribeInBackground("", new SaveCallback() {
          @Override
          public void done(ParseException e) {
              if (e == null) {
                  Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
              } else {
                  Log.e("com.parse.push", "failed to subscribe for push", e);
              }
          }
      });

    Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);
    ParseFacebookUtils.initialize(this);

  }

    public void setGroupList(ArrayList<ParseObject> list){
        this.groupList =list;
    }
    public ArrayList<ParseObject> getGroupList(){
        return this.groupList;
    }
    public void setCurrent_group(ParseObject group){ this.current_group=group;}
    public ParseObject getCurrent_group(){return this.current_group;}

    //fix-SH
    public void startMusic(AssetFileDescriptor descriptor){

        try {
            music.reset();
            music.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            music.prepare();
            music.start();
            music.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer arg0) {
                    /* ?
                    ��코드 ?��?���? 뭐�?? */
                    music.start();
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stopMusic(){
        music.stop();
    }
    public void pauseMusic(){
        music.pause();
    }

    public AssetFileDescriptor selectFile(Context context){
        try {
            AssetFileDescriptor descriptor = context.getAssets().openFd("alarm02.mp3");
            return descriptor;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateSongList() {
        //?��?��?��?��
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,soundController.AUDIO_PLAYING_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }
        songs.clear();
        File home = new File(filepath, soundController.AUDIO_PLAYING_FOLDER);
        if (home.listFiles(new WaveFilter()).length > 0) {
            for (File temp_file : home.listFiles(new WaveFilter())) {
                songs.add(filepath+"/"+soundController.AUDIO_PLAYING_FOLDER+"/"+temp_file.getName());
            }
        }
    }

    class WaveFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".wav"));
        }
    }

    public void playSong(String songPath) {
        try {
            music.reset();
            music.setDataSource(songPath);
            music.prepare();
            music.start();

            // Setup listener so next song starts automatically
            music.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer arg0) {
                    nextSong();
                }

            });

        } catch (IOException e) {
            Log.v(getString(R.string.app_name), e.getMessage());
        }
    }

    private void nextSong() {

        String filepath = Environment.getExternalStorageDirectory().getPath();
        if (++currentPosition >= songs.size()) {
            // Last song, just reset currentPosition
            currentPosition = 0;
        }
        playSong(songs.get(currentPosition));

    }

}
