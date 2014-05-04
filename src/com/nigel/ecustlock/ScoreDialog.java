package com.nigel.ecustlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ScoreDialog extends DialogFragment implements OnItemClickListener {

	TextView tvTitle = null;
	TextView tvThreshold = null;
	ListView scoreList = null;
	
	float threshold = 0;
	HashMap<String, Float> mapScore = new HashMap<String, Float>();
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View ScoreView = inflater.inflate(R.layout.dialog_mutiluser, null);
		
		builder.setView(ScoreView);
		tvTitle = (TextView) ScoreView.findViewById(R.id.score_title);
		tvThreshold = (TextView) ScoreView.findViewById(R.id.score_threshold);
		scoreList = (ListView) ScoreView.findViewById(R.id.score_list);
		
		tvThreshold.setText("阈值为：" + this.threshold);
		mListener.onSetThreshold(this);
		mListener.onSetScoreList(this);
		
		String[] adapterKeys = {"name", "score"};
		int[] adapterIds = {R.id.item_username, R.id.item_score};
		SimpleAdapter adapter = new SimpleAdapter(getActivity(), getData(), R.layout.score_item, adapterKeys, adapterIds);
		scoreList.setAdapter(adapter);
		scoreList.setOnItemClickListener(this);
		
		builder.setNegativeButton("都不是", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				// TODO add the other
				mListener.onDialogNegativeClick(ScoreDialog.this);
				ScoreDialog.this.getDialog().cancel();
			}	
		});
		
		return builder.create();
	}
	
	public interface ScoreDialogListener {
		public void onSetThreshold(DialogFragment dialog);
		public void onSetScoreList(DialogFragment dialog);
		public void onDialogNegativeClick(DialogFragment dialog);
		public void onFinish();
	}
	
	ScoreDialogListener mListener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mListener = (ScoreDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ScoreDialogListener");
		}
	}
	
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}
	
	public void setScoreList(HashMap<String, Float> mapScore) {
		this.mapScore = mapScore;
	}
	
	private List<HashMap<String, Object>> getData() {
		
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map = null;

		Iterator it = this.mapScore.keySet().iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			Float score = this.mapScore.get(name);
			map = new HashMap<String, Object>();
			map.put("name", name);
			map.put("score", score);
			if (list.size() == 0) {
				list.add(map);
			}
			else {
				boolean hasInsert = false;
				for (int i = 0; i < list.size(); i++) {
					if (score > (Float)list.get(i).get("score")) {
						list.add(i, map);
						hasInsert = true;
						break;
					}
				}
				if (!hasInsert) {
					list.add(map);
				}
			}
		}
		
		return list;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ListView listView = (ListView) parent;
		HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
		String name = (String) map.get("name");
		Float score = (Float) map.get("score");
		Toast.makeText(getActivity(), name+" "+score, Toast.LENGTH_SHORT).show();
		ScoreDialog.this.dismiss();
		mListener.onFinish();
	}
}
