package com.wedeploy.sdk;

import com.wedeploy.sdk.auth.Auth;
import com.wedeploy.sdk.auth.TokenAuth;
import com.wedeploy.sdk.exception.WeDeployException;
import com.wedeploy.sdk.internal.OkHttpTransport;
import com.wedeploy.sdk.internal.RequestMethod;
import com.wedeploy.sdk.transport.Request;
import com.wedeploy.sdk.transport.Response;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.wedeploy.sdk.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Silvio Santos
 */
public class WeDeployAuthTest {

	@BeforeClass
	public static void setUpBeforeClass() throws WeDeployException {
		deleteUsers();
		createUser();
	}

	@Test
	public void signIn() throws Exception {
		Response response = weDeploy.auth(AUTH_URL)
			.signIn(EMAIL, PASSWORD)
			.execute();

		JSONObject jsonBody = new JSONObject(response.getBody());
		String token = jsonBody.getString("access_token");

		assertNotNull(token);
	}

	@Test(expected = IllegalArgumentException.class)
	public void signIn_withNullEmail_shouldThrowException() {
		weDeploy.auth(AUTH_URL)
			.auth(AUTH)
			.signIn(null, PASSWORD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void signIn_withNullPassword_shouldThrowException() {
		weDeploy.auth(AUTH_URL)
			.auth(AUTH)
			.signIn(EMAIL, null);
	}

	@Test
	public void signOut() throws WeDeployException {
		Response response = weDeploy.auth(AUTH_URL)
			.signIn(EMAIL, PASSWORD)
			.execute();

		JSONObject jsonBody = new JSONObject(response.getBody());
		String token = jsonBody.getString("access_token");

		weDeploy.auth(AUTH_URL)
			.auth(new TokenAuth(token))
			.signOut()
			.execute();
	}

	@Test
	public void updateUser() throws Exception {
		Response response = weDeploy.auth(AUTH_URL)
			.signIn(EMAIL, PASSWORD)
			.execute();

		JSONObject jsonBody = new JSONObject(response.getBody());
		String token = jsonBody.getString("access_token");
		Auth auth = new TokenAuth(token);

		Map<String, String> fields = new HashMap<>();
		fields.put("name", "Silvio Santos 2");

		weDeploy.auth(AUTH_URL)
			.auth(auth)
			.updateUser(USER_ID, fields)
			.execute();

		response = weDeploy.auth(AUTH_URL)
			.auth(auth)
			.getCurrentUser()
			.execute();

		jsonBody = new JSONObject(response.getBody());
		assertEquals("Silvio Santos 2", jsonBody.getString("name"));

		fields.put("name", NAME);
		weDeploy.auth(AUTH_URL)
			.auth(auth)
			.updateUser(USER_ID, fields)
			.execute();
	}

	@Test
	public void sendResetPasswordEmail() throws WeDeployException {
		weDeploy.auth(AUTH_URL)
			.sendResetPasswordEmail(EMAIL)
			.execute();
	}

	private static void createUser() throws WeDeployException {
		Response response = weDeploy.auth(AUTH_URL)
			.createUser(EMAIL, PASSWORD, NAME)
			.execute();

		assertEquals(200, response.getStatusCode());

		JSONObject jsonObject = new JSONObject(response.getBody());
		USER_ID = jsonObject.getString("id");
	}

	private static void deleteUsers() {
		try {
			Request.Builder builder = new Request.Builder()
				.url(AUTH_URL)
				.method(RequestMethod.DELETE)
				.header("Authorization", "Bearer " + MASTER_TOKEN)
				.path("users");

			Call<Response> call = new Call<>(
				builder.build(), new OkHttpTransport(), new OkHttpTransport(), Response.class);

			call.execute();
		}
		catch (Exception e) {
		}
	}

	private static WeDeploy weDeploy = new WeDeploy.Builder().build();

}
