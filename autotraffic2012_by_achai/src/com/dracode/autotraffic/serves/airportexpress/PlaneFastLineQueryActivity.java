package com.dracode.autotraffic.serves.airportexpress;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.helpers.BaseActivityHelper;

public class PlaneFastLineQueryActivity extends Activity {

	protected RelativeLayout left_layout;
	protected ImageView left_img;
	protected TextView left_text;
	protected RelativeLayout right_layout;
	protected ImageView right_img;
	protected TextView right_text;
	protected TextView title;
	private BaseActivityHelper baseHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_airport_fastline_query);

		baseHelper = new BaseActivityHelper();
		baseHelper.init(this);

		initControls();
	}

	private void initControls() {
		left_layout = (RelativeLayout) findViewById(R.id.left_layout);
		if (left_layout != null) {
			left_layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}
		left_img = (ImageView) findViewById(R.id.left_img);
		left_text = (TextView) findViewById(R.id.left_text);

		right_layout = (RelativeLayout) findViewById(R.id.right_layout);
		right_img = (ImageView) findViewById(R.id.right_img);
		right_text = (TextView) findViewById(R.id.right_text);

		title = (TextView) findViewById(R.id.middle_title);

		title.setText("机场快线");

		right_img.setBackgroundResource(R.drawable.icon_alart);
		right_text.setText("历史");
	}
}
