package ca.efriesen.lydia.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ca.efriesen.lydia.R;

/**
 * Created by eric on 2013-07-12.
 */
public class MapContainerFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
		return inflater.inflate(R.layout.map_container_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);

		getFragmentManager().beginTransaction()
				.add(R.id.map_controls, new MapControlsFragment())
				.add(R.id.map_fragment, new MyMapFragment())
				.commit();
	}
}
