package com.hutu.localfile.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/**
 * 文件操作工具包
 * 
 * @author hutuxiansheng
 * 
 */
public class FileUtils {
	static String tag = "FileUtils";

	public static File createFile(String folderPath, String fileName) {
		File destDir = new File(folderPath);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		return new File(folderPath, fileName + fileName);
	}

	// get local path all files
	public static List<String> getAllFiles(File root, List<String> resultFile) {
		File[] files = root.listFiles();
		if (files == null)
			return resultFile;

		for (File f : files) {
			if (f.isDirectory()) {
//				getAllFiles(f, resultFile);//获取文件下的所有文件


			} else {
				resultFile.add(f.getAbsolutePath());
			}
		}

		return resultFile;
	}

	private static final boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	public static boolean isChinese(String strName) {
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取文件扩展名
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileFormat(String fileName) {
		if (TextUtils.isEmpty(fileName))
			return "";

		int point = fileName.lastIndexOf('.');
		return fileName.substring(point + 1);
	}

	/*
	 * 获取指定指定字符在字符串中是否出现两次以上
	 */
	public static boolean getStrCount(String source, String sub) {
		
		if (source.lastIndexOf(sub) != source.indexOf(sub) ) {
			return true;
		}

		return false;
	}

	/**
	 * 获取文件大小
	 * 
	 * @param size
	 *            字节
	 * @return
	 */
	public static String getFileSizeStr(long size) {
		if (size <= 0)
			return "0.0B";
		java.text.DecimalFormat df = new java.text.DecimalFormat("##.##");
		float temp = (float) size / 1024;
		if (temp >= 1024) {
			return df.format(temp / 1024) + "M";
		} else {
			return df.format(temp) + "K";
		}
	}

	/**
	 * 列出root目录下所有子目录
	 * 
	 * @param path
	 * @return 绝对路径
	 */
	public static List<String> listPath(String root) {
		List<String> allDir = new ArrayList<String>();
		SecurityManager checker = new SecurityManager();
		File path = new File(root);
		checker.checkRead(root);
		if (path.isDirectory()) {
			for (File f : path.listFiles()) {
				if (f.isDirectory()) {
					allDir.add(f.getAbsolutePath());
				}
			}
		}
		return allDir;
	}

	public static List<File> getChild(String root) {
		SecurityManager checker = new SecurityManager();
		File path = new File(root);
		checker.checkRead(root);
		if (path.isDirectory())
			return Arrays.asList(path.listFiles());
		else
			return null;

	}

	public static boolean isFileExist(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}

	public static boolean isDir(String filePath) {
		File file = new File(filePath);
		return file.exists() && file.isDirectory();
	}

	// 获取后缀
	public static String getExspansion(String fileName) {
		if (TextUtils.isEmpty(fileName))
			return null;
		int index = fileName.lastIndexOf(".");
		if (-1 == index || index == (fileName.length() - 1))
			return null;
		return fileName.substring(index);
	}

	public static void prepareFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * 移动文件
	 * 
	 * @param source
	 *            需要移动的文件的路径
	 * @param destination
	 *            目标路径
	 */
	public static void moveFile(String source, String destination) {
		new File(source).renameTo(new File(destination));
	}

	/**
	 * 移动文件
	 * 
	 * @param source
	 *            需要移动的文件
	 * @param destination
	 *            目标文件
	 */
	public static void moveFile(File source, File destination) {
		source.renameTo(destination);
	}

	/**
	 * 复制文件
	 * 
	 * @param src
	 *            需要复制的文件
	 * @param target
	 *            目标文件
	 */
	public static void copyFile(File src, File target) {
		// 判断是否为文件夹
		if (src.isDirectory()) {
			if (!target.exists()) {
				target.mkdir();
			}
			// 复制文件夹
			File[] currentFiles;
			currentFiles = src.listFiles();
			for (int i = 0; i < currentFiles.length; i++) {
				// 如果当前为子目录则递归
				if (currentFiles[i].isDirectory()) {
					copyFile(new File(currentFiles[i] + "/"),
							new File(target.getAbsolutePath() + "/"
									+ currentFiles[i].getName() + "/"));
				} else {
					copyFile(currentFiles[i], new File(target.getAbsolutePath()
							+ "/" + currentFiles[i].getName()));
				}
			}

		} else {
			// 创建输入输出流
			InputStream in = null;
			OutputStream out = null;
			// 创建缓存字节流
			BufferedInputStream bin = null;
			BufferedOutputStream bout = null;
			try {
				// 创建实例
				in = new FileInputStream(src);
				out = new FileOutputStream(target);
				bin = new BufferedInputStream(in);
				bout = new BufferedOutputStream(out);

				byte[] b = new byte[8192];// 用于缓存的字节数组
				int len = bin.read(b);// 获取读取到的长度
				while (len != -1)// 判断是否读取到尾部
				{
					bout.write(b, 0, len);
					len = bin.read(b);
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (bin != null) {
						bin.close();
					}
					if (bout != null) {
						bout.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	public static void delete(String filePath) {
		if (filePath == null) {
			return;
		}
		try {
			File file = new File(filePath);
			if (file == null || !file.exists()) {
				return;
			}
			if (file.isDirectory()) {
				deleteDirRecursive(file);
			} else {
				file.delete();
			}
		} catch (Exception e) {
			Log.e(tag, e.toString());
		}
	}

	/*
	 * 递归删除目录
	 */
	public static void deleteDirRecursive(File dir) {
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			return;
		}
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		for (File f : files) {
			if (f.isFile()) {
				f.delete();
			} else {
				deleteDirRecursive(f);
			}
		}
		dir.delete();
	}

	/**
	 * 判断SD卡是否已经准备好
	 * 
	 * @return 是否有SDCARD
	 */
	public static boolean isSDCardReady() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	/**
	 * 可扩展卡路径
	 * 
	 * @return
	 */
	public static String getExtSdCardPath() {
		File file = new File("/mnt/external_sd/");
		if (file.exists()) {
			return file.getAbsolutePath();
		} else {
			file = new File("/mnt/extSdCard/");
			if (file.exists())
				return file.getAbsolutePath();
		}
		return null;
	}

	public static byte[] decodeBitmap(String path) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;// 设置成了true,不占用内存，只获取bitmap宽高
		BitmapFactory.decodeFile(path, opts);
		opts.inSampleSize = computeSampleSize(opts, -1, 1024 * 800);
		opts.inJustDecodeBounds = false;// 这里一定要将其设置回false，因为之前我们将其设置成了true
		opts.inPurgeable = true;
		opts.inInputShareable = true;
		opts.inDither = false;
		opts.inPurgeable = true;
		opts.inTempStorage = new byte[16 * 1024];
		FileInputStream is = null;
		Bitmap bmp = null;
		ByteArrayOutputStream baos = null;
		try {
			is = new FileInputStream(path);
			bmp = BitmapFactory.decodeFileDescriptor(is.getFD(), null, opts);
			double scale = getScaling(opts.outWidth * opts.outHeight,
					1024 * 600);
			Bitmap bmp2 = Bitmap.createScaledBitmap(bmp,
					(int) (opts.outWidth * scale),
					(int) (opts.outHeight * scale), true);
			bmp.recycle();
			baos = new ByteArrayOutputStream();
			bmp2.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			bmp2.recycle();
			return baos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.gc();
		}
		return baos.toByteArray();
	}

	private static double getScaling(int src, int des) {
		/**
		 * 48 目标尺寸÷原尺寸 sqrt开方，得出宽高百分比 49
		 */
		double scale = Math.sqrt((double) des / (double) src);
		return scale;
	}

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}


	/**
	 * 遍历一个文件夹下的所有文件
	 */

	public  static void delHindenFile(String root) {
		SecurityManager checker = new SecurityManager();
		File path = new File(root);
		checker.checkRead(root);
		if (path.isDirectory()) {
			for (File f : path.listFiles()) {
				if (f.isDirectory()) {
					String [] a=  f.getAbsolutePath().split("/");
					String b=  a[a.length-1].substring(0,1);
					if (".".equals(b)){
						deleteAllFilesOfDir( f);
					}
				}
			}
		}
	}
	/**
	 *
	 * 删除所有文件
	 */

	public static void deleteAllFilesOfDir(File path) {
		if (!path.exists())
			return;
		if (path.isFile()) {
			path.delete();
			return;
		}
		File[] files = path.listFiles();
		for (int i = 0; i < files.length; i++) {
			deleteAllFilesOfDir(files[i]);
		}
		path.delete();
	}
}