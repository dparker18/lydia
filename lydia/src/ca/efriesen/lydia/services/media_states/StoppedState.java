package ca.efriesen.lydia.services.media_states;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import ca.efriesen.lydia.services.MediaService;

/**
 * Created by eric on 1/3/2014.
 */
public class StoppedState extends MediaState {
	public static final String TAG = "lydia stopped state";

	MediaPlayer mediaPlayer;
	MediaService mediaService;

	public StoppedState(Context context, MediaService mediaService, MediaPlayer mediaPlayer) {
		super(context, mediaService, mediaPlayer);
		this.mediaPlayer = mediaPlayer;
		this.mediaService = mediaService;
	}

	@Override
	public void play() {
		mediaService.setState(mediaService.getPlayingState());
		mediaService.getState().setSong(mediaService.playlist.get(mediaService.playlistPosition));
	}

}
