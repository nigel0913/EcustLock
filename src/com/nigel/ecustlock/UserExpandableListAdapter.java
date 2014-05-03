package com.nigel.ecustlock;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class UserExpandableListAdapter extends BaseExpandableListAdapter {

	Activity context;
	List<String> userNames;
	
	public UserExpandableListAdapter(Activity context, List<String> userNames) {
		this.context = context;
		this.userNames = userNames;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getChildId(int grosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(final int groupPosition, int childPosition, 
			boolean isLastChild, View convertView, ViewGroup parent) {
		
		LayoutInflater inflater = context.getLayoutInflater();
		
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.user_handle, null);
		}
		Log.d("ExpandListAdapter", "getChildView");
		Button btnModify = (Button) convertView.findViewById(R.id.btn_modify_user);
		Button btnDelete = (Button) convertView.findViewById(R.id.btn_delete_user);
		
		btnDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to remove?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
//                                List<String> child =
//                                    laptopCollections.get(laptops.get(groupPosition));
//                                child.remove(childPosition);
                            	String name = userNames.get(groupPosition);
                            	Log.d("deleteUser", "delete");
                            	boolean result = ((UsersActivity)context).deleteUser(groupPosition, name);
                            	if (result)
                            		userNames.remove(groupPosition);
                                notifyDataSetChanged();
                            }
                        });
                builder.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
			}
			
		});
		
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return userNames.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return userNames.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpand,
			View convertView, ViewGroup parent) {
		
		String userName = (String) getGroup(groupPosition);
		
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.user_group, null);
		}
		
		TextView item = (TextView) convertView.findViewById(R.id.tv_user_name);
		item.setText(userName);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
}
