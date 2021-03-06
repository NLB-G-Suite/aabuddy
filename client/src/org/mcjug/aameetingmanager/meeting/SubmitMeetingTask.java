package org.mcjug.aameetingmanager.meeting;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.mcjug.meetingfinder.R;
import org.mcjug.aameetingmanager.authentication.Credentials;
import org.mcjug.aameetingmanager.util.HttpUtil;
import org.mcjug.aameetingmanager.util.MeetingListUtil;

import android.content.Context;
import android.os.AsyncTask;

public class SubmitMeetingTask extends AsyncTask<Void, String, Meeting> {
    private final String TAG = getClass().getSimpleName();
    private Context context;
    private String submitMeetingParams;
	private Credentials credentials;
	private SubmitMeetingListener listener;	
	private String errorMsg =  null;

	public SubmitMeetingTask(Context context, String submitMeetingParams, Credentials credentials, SubmitMeetingListener listener) {
        this.context = context;
        this.submitMeetingParams = submitMeetingParams;
        this.credentials = credentials;
		this.listener = listener;
 	}

	@Override
	protected Meeting doInBackground(Void... arg0) {
		String errorMessage = credentials.validateCredentialsFromServer(context);
		if (errorMessage != null) {
	    	errorMsg = String.format(context.getString(R.string.validateCredentialsError), errorMessage);
			return null;
		}

		Meeting meeting = null;
		DefaultHttpClient client = HttpUtil.createHttpClient(); 
		try {
			String baseUrl = HttpUtil.getSecureRequestUrl(context, R.string.save_meeting_url_path);
			HttpPost request = new HttpPost(baseUrl);
			
	        request.addHeader("Authorization", "Basic " + credentials.getBasicAuthorizationHeader());
			
			StringEntity se = new StringEntity(submitMeetingParams);  
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			request.setEntity(se);
			
			HttpResponse response = client.execute(request);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
			    meeting = MeetingListUtil.getMeetingList(context, response).getMeetings().get(0);
			} else {
		    	errorMsg = statusLine.toString();
			}
		} catch (Exception ex) {
	    	errorMsg = ex.toString();
		} finally {
			client.getConnectionManager().shutdown();  
		}
		
		return meeting;
	}
	
	@Override
	protected void onPostExecute(Meeting meeting) {
		listener.submitMeetingResults(meeting, errorMsg);
	}
	
	public interface SubmitMeetingListener {
		public void submitMeetingResults(Meeting meeting, String errorMsg);
	}
}
