package com.looseboxes.idisc.common.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.CallSuper;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bc.android.core.io.FileIO;
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.util.PropertiesManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SumbitnewsActivity extends AbstractSingleTopActivity {

    private static final int REQUEST_CODE_SELECT_PHOTO = 100;
    private static final int REQUEST_CODE_SNAP_PHOTO = 200;

    private Uri snappedImageUri;

    private Bitmap articleImage;

    class NewfeedOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final int viewId = v.getId();
            if(viewId == R.id.newfeed_selectimagebutton) {
                SumbitnewsActivity.this.selectImage(v);
            }else if(viewId == R.id.newfeed_snapimagebutton) {
                SumbitnewsActivity.this.snapImage(v);
            }else if(viewId == R.id.newfeed_okbutton) {
                SumbitnewsActivity.this.processInputs(v);
            }else{
                throw new UnsupportedOperationException(this.getClass().getName() +
                        " not expecting view of type: " + v.getClass().getSimpleName() +
                        " with id: " + viewId + " with text: " +
                        (v instanceof TextView ? ((TextView)v).getText() : null)
                );
            }
        }
    }

    public int getContentViewId() {
        return R.layout.newfeed;
    }

    @Override
    @CallSuper
    protected void onDestroy() {
        super.onDestroy();
        if(this.snappedImageUri != null) {

        }
        if(this.articleImage != null) {
            this.articleImage.recycle();
            this.articleImage = null;
        }
    }

    protected void doCreate(Bundle icicle) {

        super.doCreate(icicle);

        final String [] categoriesArray = this.loadCategories();
        final Spinner spinner = (Spinner)this.findViewById(R.id.newfeed_categories);
        this.initSpinner(spinner, categoriesArray);

        View.OnClickListener onClickListener = new NewfeedOnClickListener();

        View selectImage = this.findViewById(R.id.newfeed_selectimagebutton);
        selectImage.setOnClickListener(onClickListener);

        View snapImage = this.findViewById(R.id.newfeed_snapimagebutton);
        snapImage.setOnClickListener(onClickListener);

        View okButton = this.findViewById(R.id.newfeed_okbutton);
        okButton.setOnClickListener(onClickListener);
    }

    private void initSpinner(Spinner spinner, String[] entries) {

        List list = Arrays.asList(entries);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, list
        );

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);
    }

    private void selectImage(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_CODE_SELECT_PHOTO);
    }

    private void snapImage(View view) {

        final String dirName = "saved_images";

        final String imageName = "image"+Long.toHexString(System.currentTimeMillis())+".png";

        final File imageFile = FileIO.getExternalPublicFile(this, dirName, imageName, true);

        if(imageFile.exists()) {
            imageFile.delete();
        }

        this.snappedImageUri = Uri.fromFile(imageFile);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, snappedImageUri);

        startActivityForResult(takePictureIntent, REQUEST_CODE_SNAP_PHOTO);
    }

    private void processInputs(View v) {

        TextView titleView = (TextView)this.findViewById(R.id.newfeed_title);
        CharSequence title = titleView.getText();

        Spinner spinner = (Spinner)this.findViewById(R.id.newfeed_categories);
        Object category = spinner.getSelectedItem();

        TextView contentView = (TextView)this.findViewById(R.id.newfeed_content);
        CharSequence content = contentView.getText();

        Popup.getInstance().show(this, "Title: " + title + "\nCategory: " + category + "\nContent: " + content, Toast.LENGTH_SHORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {

        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        try {
            switch (requestCode) {
                case REQUEST_CODE_SELECT_PHOTO:
                    this.processSelectImageResult(resultCode, imageReturnedIntent);
                    break;
                case REQUEST_CODE_SNAP_PHOTO:
                    this.processSnappedImageResult(resultCode, imageReturnedIntent);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }catch(FileNotFoundException e) {
            this.handleException(e, R.string.err_unexpected);
        }catch(RuntimeException e) {
            this.handleException(e, R.string.err_unexpected);
        }
    }

    private void handleException(Exception e, int msg) {
        Logx.getInstance().log(this.getClass(), e);
        Popup.getInstance().show(this, msg, Toast.LENGTH_LONG);
    }

    private void processSelectImageResult(int resultCode, Intent imageReturnedIntent)
    throws FileNotFoundException {
        if (resultCode == RESULT_OK) {

            this.addSelectedImage(imageReturnedIntent.getData());
        }
    }

    private void processSnappedImageResult(int resultCode, Intent imageReturnedIntent)
            throws FileNotFoundException{
        if (resultCode == RESULT_OK) {

            this.addSelectedImage(this.snappedImageUri);
        }
    }

    private void addSelectedImage(Uri imageUri) throws FileNotFoundException {

        final Bitmap selectedImage = this.getBitmap(this, imageUri);

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(selectedImage);
        Dialog.OnClickListener listener = new Dialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case Dialog.BUTTON_POSITIVE:
                        if(articleImage != null) {
                            articleImage.recycle();
                        }
                        articleImage = selectedImage;
                        break;
                }
                dialog.dismiss();
            }
        };
        AlertDialog.Builder builder = Popup.getInstance().getBuilder(
                this, R.string.msg_acceptimage, listener, R.string.msg_yes, Popup.NO_RES, R.string.msg_no);
        builder.setView(imageView);
        builder.create().show();
    }

    private Bitmap getBitmap(Context context, Uri imageUri) throws FileNotFoundException {
        Bitmap output;
//        output = new PicassoImageManagerImpl(context).decodeImageUri(imageUri, 140);
        InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
        output = BitmapFactory.decodeStream(imageStream);
        return output;
    }

    private String [] loadCategories() {
        final Set<String> categories = (Set<String>)App.getPropertiesManager(this).getSet(PropertiesManager.PropertyName.categories);
        return categories.toArray(new String[0]);
    }
}
/**
 *
 public void showImage(Context context, Bitmap imageBitmap) {
 Dialog dialog = new Dialog(context);
 dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 dialog.getWindow().setBackgroundDrawable(
 new ColorDrawable(android.graphics.Color.TRANSPARENT));
 ImageView imageView = new ImageView(this);
 imageView.setImageBitmap(imageBitmap);
 dialog.addContentView(imageView, new RelativeLayout.LayoutParams(
 ViewGroup.LayoutParams.MATCH_PARENT,
 ViewGroup.LayoutParams.MATCH_PARENT));
 dialog.show();
 }
 *
 */

