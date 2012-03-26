package com.dracode.autotraffic.nearby;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dracode.andrdce.ct.AppUtil;
import com.dracode.andrdce.ct.TypeUtil;
import com.dracode.autotraffic.R;
import com.dracode.autotraffic.common.map.PoiListActivity;

public class NearbySortAdapter extends BaseExpandableListAdapter {

	private Context ctx;
	/** 分类. */
	private List<Map<String, Object>> groups;
	/** 搜索距离 . */
	public int distance;
	/** 图片缓存 . */
	private Map<String, Object> imgCacheMap;

	public NearbySortAdapter(Context ctx, List<Map<String, Object>> groups,
			int distance, Map<String, Object> imgCacheMap) {
		this.ctx = ctx;
		this.groups = groups;
		this.distance = distance;
		this.imgCacheMap = imgCacheMap;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		@SuppressWarnings("unchecked")
		List<Object> childs = (List<Object>) groups.get(groupPosition).get(
				"childs");
		return childs.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		List<Map<String, Object>> childs = TypeUtil.CastToList_SO(groups.get(
				groupPosition).get("childs"));
		return childs.size();
	}

	// 取子列表中的某一项的 View
	@Override
	public View getChildView(final int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		LayoutInflater inflate = LayoutInflater.from(ctx);
		View view = inflate.inflate(R.layout.nearby_query_second_item, null);

		final Map<String, String> typeEntity = TypeUtil.CastToMap_SS(getChild(
				groupPosition, childPosition));

		TextView textView = (TextView) view.findViewById(R.id.text);
		textView.setText(typeEntity.get("NAME"));

		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String _poiType = "";
				if (typeEntity.get("POITYPE") == null
						|| typeEntity.get("POITYPE").length() == 0) {
					_poiType = typeEntity.get("NAME") + "|";
				} else {
					_poiType = typeEntity.get("POITYPE");
				}
				Bundle bundle = new Bundle();
				bundle.putString("poiType", _poiType);
				bundle.putInt("distance", distance);
				bundle.putString("keywords", typeEntity.get("NAME"));
				AppUtil.startActivity(ctx, PoiListActivity.class, false, bundle);
			}
		});
		return view;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	// 取父列表中的某一项的 View
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		LayoutInflater inflate = LayoutInflater.from(ctx);
		View view = inflate.inflate(R.layout.nearby_query_item, null);

		final Map<String, String> typeEntity = TypeUtil
				.CastToMap_SS(getGroup(groupPosition));

		Drawable drawable = null;
		try {
			String img = typeEntity.get("IMGURL");
			if (img != null && img.length() > 0 && imgCacheMap!=null) {
				Object buf = imgCacheMap.get(img);
				if (buf != null)
					drawable = Drawable.createFromStream(
							new ByteArrayInputStream((byte[]) buf), null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		TextView textView = (TextView) view.findViewById(R.id.name);
		textView.setText(typeEntity.get("NAME"));
		ImageView imageView = (ImageView) view.findViewById(R.id.icon);
		imageView.setBackgroundDrawable(drawable);

		ImageView imgeView = (ImageView) view.findViewById(R.id.arraw);
		if ("1".equals(typeEntity.get("isOpen"))) {
			imgeView.setBackgroundResource(R.drawable.arrow03);
		} else {
			imgeView.setBackgroundResource(R.drawable.arrow01);
		}
		if (groupPosition == (groups.size() - 1)) {
			TextView line = (TextView) view.findViewById(R.id.line);
			line.setVisibility(View.VISIBLE);
		}
		return view;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
