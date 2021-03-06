package us.koller.cameraroll;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import us.koller.cameraroll.data.AlbumItem;
import us.koller.cameraroll.data.Video;
import us.koller.cameraroll.ui.ItemActivity;
import us.koller.cameraroll.data.Album;
import us.koller.cameraroll.ui.MainActivity;
import us.koller.cameraroll.ui.VideoPlayerActivity;
import us.koller.cameraroll.util.Util;

public class IntentReceiver extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switch (getIntent().getAction()) {
            case Intent.ACTION_VIEW:
                view(getIntent());
                this.finish();
                break;
            case Intent.ACTION_PICK:
                pick(getIntent());
                break;
            case Intent.ACTION_GET_CONTENT:
                pick(getIntent());
                break;
        }
    }

    public void view(Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) {
            Toast.makeText(this, getString(R.string.error) + ": Uri = null", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }

        String path = uri.toString();
        int index = path.lastIndexOf("/");

        Album album = new Album().setPath(path.substring(0, index));
        AlbumItem albumItem = AlbumItem.getInstance(this, path);
        if (albumItem == null) {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        album.getAlbumItems().add(albumItem);

        if (albumItem instanceof Video) {
            Intent view_video = new Intent(this, VideoPlayerActivity.class)
                    .setData(uri);
            startActivity(view_video);
        } else {
            Util.printIntentFlags(intent);

            Intent view_photo = new Intent(this, ItemActivity.class)
                    .setData(uri)
                    .putExtra(ItemActivity.ALBUM_ITEM, albumItem)
                    .putExtra(ItemActivity.VIEW_ONLY, true)
                    .putExtra(ItemActivity.ALBUM, album)
                    .putExtra(ItemActivity.ITEM_POSITION, album.getAlbumItems().indexOf(albumItem))
                    .putExtra(ItemActivity.FINISH_AFTER, true)
                    .addFlags(intent.getFlags());
            startActivity(view_photo);
        }
    }

    public void pick(Intent intent) {
        setIntent(new Intent("ACTIVITY_ALREADY_LAUNCHED"));

        Intent pick_photos = new Intent(this, MainActivity.class)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false))
                .setAction(MainActivity.PICK_PHOTOS);

        startActivityForResult(pick_photos, MainActivity.PICK_PHOTOS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MainActivity.PICK_PHOTOS_REQUEST_CODE:
                if (resultCode != RESULT_CANCELED) {
                    setResult(RESULT_OK, data);
                }
                this.finish();
                break;
        }
    }
}
