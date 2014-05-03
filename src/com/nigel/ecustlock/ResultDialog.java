package com.nigel.ecustlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ResultDialog extends DialogFragment {

	TextView tvScore = null;
	TextView tvThreshold = null;
	TextView tvName = null;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    final View ResultView = inflater.inflate(R.layout.dialog_result, null);
	    
	    builder.setView(ResultView);
	    tvScore = (TextView) ResultView.findViewById(R.id.result_score);
	    tvThreshold = (TextView) ResultView.findViewById(R.id.result_threshold);
	    tvName = (TextView) ResultView.findViewById(R.id.result_trainer);
	    mListener.onSetScore();
	    // Add action buttons
	    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   // sign in the user ...
	            	   mListener.onDialogPositiveClick(ResultDialog.this);
	               }
	           })
	           .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   mListener.onDialogNegativeClick(ResultDialog.this);
	               }
	           });
	    return builder.create();
	}
	
	
	public interface ResultDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
        public void onSetScore();
	}
	
	ResultDialogListener mListener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			// Instantiate the NoticeDialogListener so we can send events to the host
			mListener = (ResultDialogListener) activity;
		} catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement ResultDialogListener");
        }
	}
	
	public void UpdateScoreView(double score, double threshold, String trainer) {
		Log.d("UpdateScoreView", "" + score);
		tvScore.setText(String.format("得分为：%.2f", score));
		tvThreshold.setText(String.format("阈值为：%.2f", threshold));
		tvName.setText("你是"+trainer+"吗？");
	}
	
}
