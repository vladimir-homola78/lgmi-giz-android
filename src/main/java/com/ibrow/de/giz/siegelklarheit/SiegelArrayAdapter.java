package com.ibrow.de.giz.siegelklarheit;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ibrow.de.giz.siegelklarheit.LogoHelper.getFromMemoryCache;

/**
 * A an adaptor for list of Siegels, used in list views.
 *
 * Uses LogoLoaderTask and therfore LogoHelper.
 *
 * @see LogoHelper
 * @see LogoLoaderTask
 * @author Pete
 */
public class SiegelArrayAdapter extends ArrayAdapter<ShortSiegelInfo> {

    private final Context context;
    private final ShortSiegelInfo[] siegels;
    private final String packageName;
    private final Resources resources;

    protected Drawable blankLogo;

    public SiegelArrayAdapter(Context context, ShortSiegelInfo[] values) {
        super(context, R.layout.listitem_siegel, values);
        this.context = context;
        this.siegels = values;
        packageName = context.getPackageName();
        resources = context.getResources();
        blankLogo = context.getResources().getDrawable(R.drawable.blank_label_logo);
        LogoHelper.initDiskCachePath(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.listitem_siegel, parent, false);

        ShortSiegelInfo siegel = siegels[position];
        SiegelRating rating = siegel.getRating();

        ImageView rating_image_view = (ImageView) rowView.findViewById(R.id.rating_symbol_image);
        rating_image_view.setImageDrawable( resources.getDrawable(resources.getIdentifier("@drawable/" + rating.getImageIdentifier(), null, packageName)) );

        TextView rating_color = (TextView)  rowView.findViewById(R.id.rating_color);
        rating_color.setBackgroundColor(rating.getColor());

        TextView title_view = (TextView)  rowView.findViewById(R.id.siegel_name_text);
        title_view.setText(siegel.getName());

        ImageView logo_image_view = (ImageView) rowView.findViewById(R.id.logo_view);
        Bitmap image = getFromMemoryCache(siegel);
        if(image != null){
            logo_image_view.setImageBitmap(image);
        }
        else {
            LoadSiegelLogoTask logo_task = new LoadSiegelLogoTask(logo_image_view, siegel);
            logo_task.execute(siegel);
        }

        switch (siegel.getRating()) {
            case NONE:
                TextView no_criteria_view = (TextView)  rowView.findViewById(R.id.siegel_no_criteria_text);
                no_criteria_view.setText(R.string.criterion_none);
                LinearLayout critertia_view = (LinearLayout) rowView.findViewById(R.id.criteria_holder);
                critertia_view.setVisibility(View.GONE);
                no_criteria_view.setVisibility(View.VISIBLE);
                break;

            case UNKNOWN:
                TextView no_criteria_view2 = (TextView)  rowView.findViewById(R.id.siegel_no_criteria_text);
                no_criteria_view2.setText(R.string.criterion_unknown);
                LinearLayout critertia_view2 = (LinearLayout) rowView.findViewById(R.id.criteria_holder);
                critertia_view2.setVisibility(View.GONE);
                no_criteria_view2.setVisibility(View.VISIBLE);
                break;

            default:
                List<Criterion> critertia=siegel.getCriteria();
                Criterion c = null;
                TextView criteria_text=null;
                ImageView criteria_image=null;
                int critertia_quantity = critertia.size();
                int id=-1;
                for(int i=0; i<IdentifeyeAPIInterface.EXPECTED_CRITERIA_NUMBER; i++){
                    if(i<critertia_quantity) {
                        c = (Criterion) critertia.get(i);

                        if( c!=null ) {
                            id=i+1;
                            criteria_text = (TextView) rowView.findViewById(resources.getIdentifier("@id/criteria_text_" + id, null, packageName));
                            criteria_text.setText(resources.getText(
                                            resources.getIdentifier("@string/" + c.getType().getNameIdentifier(), null, packageName)
                                    )
                            );

                            criteria_image = (ImageView) rowView.findViewById(resources.getIdentifier("@id/criteria_image_" + id, null, packageName));
                            criteria_image.setImageDrawable(resources.getDrawable(resources.getIdentifier("@drawable/" + c.getValue().getNameIdentifierForImage(), null, packageName)));
                        }
                        else{
                            android.util.Log.e("SiegelAdapter", "Null criterium");
                        }
                    }
                } // end loop
                break;
        } // end switch

        LinearLayout info_holder = (LinearLayout) rowView.findViewById(R.id.info_holder);
        FrameLayout rating_holder = (FrameLayout) rowView.findViewById(R.id.rating_holder);
        //rating_holder.setMinimumHeight( info_holder.getHeight() );
        //rating_image_view.setMinimumHeight( info_holder.getHeight() );
        //rating_color.setMinimumHeight( info_holder.getHeight() );
        android.util.Log.d("SiegelAdapter", "info holder hieght is "+info_holder.getHeight());
        //rating_color.setHeight( info_holder.getHeight() );

        return rowView;
    }




    private class LoadSiegelLogoTask extends LogoLoaderTask{

        private boolean gotImage=false;
        private final ImageView logoView;
        private final ShortSiegelInfo siegel;

        LoadSiegelLogoTask(final ImageView logo_view, ShortSiegelInfo siegel){
            this.logoView = logo_view;
            this.siegel = siegel;
        }

        @Override
        protected void onProgressUpdate(Bitmap... progress) {
            logoView.setImageBitmap(progress[0]);
            gotImage=true;
        }

        protected void onPostExecute(Void result){
            if( isCancelled() ){
                return;
            }
            if(! gotImage ){
                logoView.setImageDrawable(blankLogo);
            }
        }
    }

}
