package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.utils.AzureUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.abinbev.dsa.utils.AzureUtils.getAzurePhotoFileForAccount;

public class ProspectPhotoField extends LinearLayout {

    public static final String TAG = ProspectPhotoField.class.getSimpleName();

    public interface ProspectsPhotoFieldListener {
        void onPhotoFieldClicked(View view);
    }


    @Bind(R.id.field_name)
    TextView fieldTextView;

    @Bind(R.id.hero_image)
    ImageView heroImage;

    @Bind(R.id.take_picture_button)
    View takePictureButton;

    @Bind(R.id.take_picture_button_big)
    View takePictureBigButton;
    @Bind(R.id.tv_necessary)
    TextView necessaryTv;


    private Uri imageUri;

    private ProspectsPhotoFieldListener listener;

    public ProspectPhotoField(Context context, int fieldNameRes) {
        super(context);
        inflate(context, R.layout.prospect_photo_field, this);
        ButterKnife.bind(this);
        fieldTextView.setText(fieldNameRes);
        showBigTakePictureButton();
    }

    public void setListener(ProspectsPhotoFieldListener listener) {
        this.listener = listener;
    }

    public String getFieldName() {
        return fieldTextView.getText().toString();
    }

    public Uri getImageUri() {
        return imageUri;
    }

    private void showBigTakePictureButton() {
        takePictureButton.setVisibility(GONE);
        takePictureBigButton.setVisibility(VISIBLE);
    }

    private void showSmallTakePictureButton() {
        takePictureButton.setVisibility(VISIBLE);
        takePictureBigButton.setVisibility(GONE);
    }

    public void setVisibility(int visibility) {
        necessaryTv.setVisibility(visibility);
    }

    @OnClick({R.id.take_picture_button, R.id.take_picture_button_big})
    public void onTakePictureClicked() {
        this.listener.onPhotoFieldClicked(this);
    }

    public void setPhoto(Uri imageUri) {
        if (TextUtils.isEmpty(imageUri.getPath())) {
            this.imageUri = null;
            Picasso.with(getContext())
                    .load(R.drawable.bg_empty_photo)
                    .fit()
                    .centerCrop()
                    .into(heroImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            showBigTakePictureButton();
                        }

                        @Override
                        public void onError() {
                            showBigTakePictureButton();
                        }
                    });
        } else {
            this.imageUri = imageUri;
            Picasso.with(getContext())
                    .load(this.imageUri)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .placeholder(R.drawable.bg_empty_photo)
                    .error(R.drawable.bg_empty_photo)
                    .fit()
                    .centerCrop()
                    .into(heroImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            showSmallTakePictureButton();
                        }

                        @Override
                        public void onError() {
                            showBigTakePictureButton();
                        }
                    });
        }
    }

    public void setPhoto(Attachment attachment, String accountId) {
        Log.v(TAG, "setPhoto...:" + attachment.getBodyLength());

        if (attachment == null) {
            final File azurePhotoForAccount = getAzurePhotoFileForAccount(accountId);

            if (azurePhotoForAccount.exists()) {
                Log.v(TAG, "... set azure local file: " + azurePhotoForAccount.getPath());
                Picasso.with(getContext())
                        .load(azurePhotoForAccount)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .placeholder(R.drawable.bg_empty_photo)
                        .error(R.drawable.bg_empty_photo)
                        .fit()
                        .centerCrop()
                        .into(heroImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                showSmallTakePictureButton();
                            }

                            @Override
                            public void onError() {
                                showBigTakePictureButton();
                            }
                        });
                return;
            } else {
                String completeUrl = AzureUtils.getPathToAccountPhotoInAzure(accountId);
                Log.v(TAG, "... set azure remote file: " + completeUrl);
                Picasso.with(getContext())
                        .load(completeUrl)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .placeholder(R.drawable.bg_empty_photo)
                        .error(R.drawable.bg_empty_photo)
                        .fit()
                        .centerCrop()
                        .into(heroImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                showSmallTakePictureButton();
                            }

                            @Override
                            public void onError() {
                                showBigTakePictureButton();
                            }
                        });
                if (!completeUrl.equals("")) {
                    return;
                }
            }
        }

        if (TextUtils.isEmpty(accountId)) {
            Log.v(TAG, "... set default background");
            Picasso.with(getContext())
                    .load(R.drawable.bg_empty_photo)
                    .fit()
                    .centerCrop()
                    .into(heroImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            showBigTakePictureButton();
                        }

                        @Override
                        public void onError() {
                            showBigTakePictureButton();
                        }
                    });
        }
        // Attachment is not in database, it is only local file.
        else if (TextUtils.isEmpty(attachment.getId())) {
            String path = attachment.getFilePath(getContext(), accountId);
            Log.v(TAG, "... set local file: " + path);
            Picasso.with(getContext())
                    .load("file://" + path)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .placeholder(R.drawable.bg_empty_photo)
                    .error(R.drawable.bg_empty_photo)
                    .fit()
                    .centerCrop()
                    .into(heroImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            showSmallTakePictureButton();
                        }

                        @Override
                        public void onError() {
                            showBigTakePictureButton();
                        }
                    });
        } else {
            Log.v(TAG, "... set attachment " + accountId + "/" + attachment.getId());
            Picasso.with(getContext())
                    .load("attachment://" + accountId + "/" + attachment.getId())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .placeholder(R.drawable.bg_empty_photo)
                    .error(R.drawable.bg_empty_photo)
                    .fit()
                    .centerCrop()
                    .into(heroImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            showSmallTakePictureButton();
                        }

                        @Override
                        public void onError() {
                            showBigTakePictureButton();
                        }
                    });
        }

    }
}