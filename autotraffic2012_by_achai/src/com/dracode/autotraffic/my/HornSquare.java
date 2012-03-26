package com.dracode.autotraffic.my;

public class HornSquare {

	/** logo图片地址.*/
	private String logo_url;
	/** 商家名.*/
	private String business_name;
	/** 发表的时间.*/
	private String date_time;
	/** 信息内容或评论.*/
	private String content;
	/** 发布的图片.*/
	private String comment_pic_url;
	/** 评论的次数.*/
	private int comment_times;
	/** 转发的次数.*/
	private int transmit_times;
	/** 回复微博的内容.*/
	private String respand_message;
	public String getLogo_url() {
		return logo_url;
	}
	public void setLogo_url(String logoUrl) {
		logo_url = logoUrl;
	}
	public String getBusiness_name() {
		return business_name;
	}
	public void setBusiness_name(String businessName) {
		business_name = businessName;
	}
	public String getDate_time() {
		return date_time;
	}
	public void setDate_time(String dateTime) {
		date_time = dateTime;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getComment_pic_url() {
		return comment_pic_url;
	}
	public void setComment_pic_url(String commentPicUrl) {
		comment_pic_url = commentPicUrl;
	}
	public int getComment_times() {
		return comment_times;
	}
	public void setComment_times(int commentTimes) {
		comment_times = commentTimes;
	}
	public int getTransmit_times() {
		return transmit_times;
	}
	public void setTransmit_times(int transmitTimes) {
		transmit_times = transmitTimes;
	}
	public String getRespand_message() {
		return respand_message;
	}
	public void setRespand_message(String respandMessage) {
		respand_message = respandMessage;
	}
	
}
