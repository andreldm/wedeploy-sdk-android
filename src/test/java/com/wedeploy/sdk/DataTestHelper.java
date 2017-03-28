package com.wedeploy.sdk;

import com.wedeploy.sdk.exception.WeDeployException;
import org.json.JSONArray;

import java.io.InputStream;
import java.util.Scanner;

import static com.wedeploy.sdk.Constants.AUTH;
import static com.wedeploy.sdk.Constants.DATA_URL;

/**
 * @author Silvio Santos
 */
public class DataTestHelper {

	private static WeDeploy weDeploy = new WeDeploy.Builder().build();

	public static void deleteData() {
		try {
			weDeploy.data(DATA_URL)
				.auth(AUTH)
				.delete("messages")
				.execute();
		}
		catch(WeDeployException e) {
		}
	}

	public static void initDataFromFile(String path) throws WeDeployException {
		deleteData();

		InputStream is = WeDeployDataTest.class.getClassLoader()
			.getResourceAsStream(path);

		String json = new Scanner(is, "UTF-8")
			.useDelimiter("\\A")
			.next();

		weDeploy.data(DATA_URL)
			.auth(AUTH)
			.create("messages", new JSONArray(json))
			.execute();
	}
}
