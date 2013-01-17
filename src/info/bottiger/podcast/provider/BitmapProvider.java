package info.bottiger.podcast.provider;

import info.bottiger.podcast.R;
import info.bottiger.podcast.service.PodcastDownloadManager;
import info.bottiger.podcast.utils.SDCardManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaMetadataRetriever;

public class BitmapProvider {

	private Context mContext;
	private WithIcon mItem;

	private DiskLruImageCache mDiskLruImageCache; // From
													// https://github.com/JakeWharton/DiskLruCache
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private final CompressFormat mCompressFormat = CompressFormat.PNG;
	private final int mCompressQuality = 70;

	private static final String DISK_CACHE_SUBDIR = "thumbnails";

	/*
	 * Class for extracting, downloading, caching and resizing bitmaps.
	 */
	public BitmapProvider(Context context, WithIcon item) {
		super();
		this.mContext = context;
		this.mItem = item;

		mDiskLruImageCache = new DiskLruImageCache(context, DISK_CACHE_SUBDIR,
				DISK_CACHE_SIZE, mCompressFormat, mCompressQuality);
	}

	/*
	 * @return The path to the Items icons
	 */
	public String getThumbnailPath() {

		/* Calculate the imagePath */
		String imageURL = null;
		Bitmap generatedFile = null;
		File thumbnail = getThumbnailFile();
		
		// make sure mItem is defined
		if (mItem == null) return defaultIcon().getAbsolutePath();

		// 1. Attempt: Extract thumbnail from local file.
		//
		// Test if the file is downloaded and everything is okay
		// Less confusing test would be appreciated
		if (mItem instanceof FeedItem) {
			FeedItem feedItem = (FeedItem) mItem;
			if (mediaFileExist(feedItem)) {

				// create the thumbnail if it doesn't exist
				if (!thumbnail.exists()) {
					generatedFile = bitmapFromFile(thumbnail);
				}

				if (thumbnail.exists() || generatedFile != null) {
					String urlPrefix = "file://";
					String thumbnailPath = thumbnailCacheURL(mItem);
					return imageURL = urlPrefix + thumbnailPath;
				}
			}
		}

		// 2. Attempt: Extract thumbnail from remote file.
		/*
		 * generatedFile = bitmapFromFile(new File(mItem.image)); if
		 * (generatedFile != null) { return thumbnail.getAbsolutePath(); }
		 */

		// 3. Attempt: Extract thumbnail from podcast feed.
		imageURL = mItem.getImageURL(mContext);
		if (imageURL != null)
			return imageURL;
		
		// 4. Attempt: If everything fails return a dummy image.
		return defaultIcon().getAbsolutePath();
		
	}

	public Bitmap createBitmapFromMediaFile() {
		return bitmapFromFile(getThumbnailFile());
	}

	/*
	 * Extracts the Bitmap from the MP3/media file
	 */
	public Bitmap createBitmapFromMediaFile(FileDescriptor fd) {

		String cacheKey = thumbnailCacheName(mItem);

		// Return if we have the bitmap cached
		if (mDiskLruImageCache.containsKey(cacheKey))
			return mDiskLruImageCache.getBitmap(cacheKey);

		// if (mediaFileExist()) {
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		mmr.setDataSource(fd);
		byte[] embeddedPicture = mmr.getEmbeddedPicture();

		// If the image exists, cache and return
		if (embeddedPicture != null) {
			Bitmap cover = BitmapFactory.decodeByteArray(embeddedPicture, 0,
					embeddedPicture.length);
			mDiskLruImageCache.put(cacheKey, cover);

			try {
				saveBitmap(cover, getThumbnailFile());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}

			return cover;
		}
		// }

		// If we could not extract the bitmap from the media file - return a
		// dummy image
		// Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(),
		// R.drawable.soundwaves);
		return null;
	}

	/*
	 * Saves the bitmap to a given file.
	 */
	public void saveBitmap(Bitmap bitmap, File file)
			throws FileNotFoundException {
		FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
		bitmap.compress(mCompressFormat, mCompressQuality, out);
	}

	/*
	 * 
	 */
	private File getThumbnailFile() {
		String thumbnailPath = thumbnailCacheURL(mItem);
		return new File(thumbnailPath);
	}

	private String thumbnailCacheURL(WithIcon item) {
		String thumbURL = SDCardManager.getThumbnailCacheDir() + "/"
				+ thumbnailCacheName(item) + ".png";
		return thumbURL;
	}

	@SuppressLint("UseValueOf")
	private String thumbnailCacheName(WithIcon item) {
		String type = null;

		if (item instanceof FeedItem) {
			FeedItem feedItem = (FeedItem) item;
			PodcastDownloadManager.DownloadStatus ds = PodcastDownloadManager
					.getStatus(feedItem);
			switch (ds) {
			case DONE:
				// file icon
				type = "local";
				break;
			default:
				// feed icon
				type = "remote";
				break;
			}
		} else {
			type = "subscription";
		}

		String StringID = new Long(item.getId()).toString();
		return StringID + "_" + type;

	}

	/*
	 * Test if the media file is on disk.
	 */
	private boolean mediaFileExist(FeedItem feedItem) {
		String fullPath = SDCardManager
				.pathFromFilename(feedItem.getPathname());
		PodcastDownloadManager.DownloadStatus ds = PodcastDownloadManager
				.getStatus(feedItem);

		return feedItem.getPathname() != null
				&& feedItem.getPathname().length() > 0
				&& new File(fullPath).exists()
				&& ds == PodcastDownloadManager.DownloadStatus.DONE;
	}

	@SuppressWarnings("resource")
	private Bitmap bitmapFromFile(File file) {
		FileInputStream fis;
		Bitmap generatedFile = null;

		try {
			fis = new FileInputStream(file);
			generatedFile = createBitmapFromMediaFile(fis.getFD());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return generatedFile;
	}
	
	private File defaultIcon() {
		String path = SDCardManager.pathFromFilename("default");
		File defaultFile = new File(path);
		if (defaultFile.exists())
			return defaultFile;
		
		Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.soundwaves);
		try {
			saveBitmap(icon, defaultFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return defaultFile;
	}
}
