package com.byslin.cordova.plugin;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

import io.cordova.hellocordova.R;

public class Util {


    public static SoundPool sp;
    public static Map<Integer, Integer> suondMap;
    public static Context context;

    //init sound pool
    public static void initSoundPool(Context context) {
        Util.context = context;
        sp = new SoundPool
                .Builder()
                .setMaxStreams(1)
                .setAudioAttributes(
                        new AudioAttributes
                                .Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build())
                .build();
        suondMap = new HashMap<Integer, Integer>();
        suondMap.put(1, sp.load(context, R.raw.msg, 1));
    }

    //play sound
    public static void play(int sound, int number) {
        AudioManager am = (AudioManager) Util.context.getSystemService(Util.context.AUDIO_SERVICE);
        //return AlarmManager The largest volume at present
        float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        //return AlarmManager The largest volume at present
        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumnRatio = audioCurrentVolume / audioMaxVolume;
        sp.play(1, 1, 1, 0, 0, 1);//0.5-2.0 speed
    }

}
