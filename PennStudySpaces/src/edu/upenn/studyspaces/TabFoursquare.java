package edu.upenn.studyspaces;


import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TabFoursquare extends Fragment{

	private StudySpace o;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tabfoursquare, container, false);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		super.onActivityCreated(savedInstanceState);
		Intent i = getActivity().getIntent();
		o = (StudySpace) i.getSerializableExtra("STUDYSPACE");
		
		TextView tt = (TextView) getView().findViewById(R.id.spacename);
		tt.setText(o.getBuildingName());
		
		TextView rt = (TextView) getView().findViewById(R.id.roomtype);
		rt.setText(o.getSpaceName());
		
		ImageView image = (ImageView) getView().findViewById(R.id.imageID);
	    if(image!=null){
	 	   Resources resource = getResources();
	 	   if(SpaceInfo.getPicture(o).length()!=0){
	 		   image.setVisibility(View.VISIBLE);
	 		   int resID = resource.getIdentifier(SpaceInfo.getPicture(o), "drawable", getActivity().getPackageName() );
	 		   image.setImageResource(resID);
	 	   }else{
	 		   image.setVisibility(View.GONE);
	 	   }
	    }
	    
	    TextView descr = (TextView) getView().findViewById(R.id.details);
	    if(descr!=null){
	    	if(SpaceInfo.getDescription(o).equals("")){
	    		descr.setVisibility(View.GONE);
	    	}else{
	    		descr.setText(SpaceInfo.getDescription(o));
	    		descr.setVisibility(View.VISIBLE);
	    	}
	    }
		
	    if(o.getFoursquare().length>0){
	    	TextView t = (TextView) getView().findViewById(R.id.tips);
	    	if(t!=null){
	    		t.setVisibility(View.VISIBLE);
	    	}
	    	
	    	String[] tips = o.getFoursquare();
	    	TextView tipslist = (TextView) getView().findViewById(R.id.tipslist);
	    	
	    	String listOfTips="";
    		if(tipslist!=null){
    			for(String s: tips){
	    			listOfTips += "• "+s+"\n\n";
	    		}
    			tipslist.setText(listOfTips);
	    	}
		}else{
			TextView t = (TextView) getView().findViewById(R.id.tips);
	    	if(t!=null){
	    		t.setVisibility(View.GONE);
	    	}
	    	
	    	TextView tipslist = (TextView) getView().findViewById(R.id.tipslist);
	    	if(tipslist!=null){
	    		tipslist.setVisibility(View.GONE);
	    	}
		}
	}
	
	public void onFoursquareClick(View v){
		Intent k = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.foursquare.com/places"));
		startActivity(k);
	}
}
