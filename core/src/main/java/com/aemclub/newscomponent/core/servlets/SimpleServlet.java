/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aemclub.newscomponent.core.servlets;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */
@SuppressWarnings("serial")
@SlingServlet(paths = "/bin/getMorningNews", methods = "GET")
public class SimpleServlet extends SlingSafeMethodsServlet {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SimpleServlet.class);

	@Override
	protected void doGet(final SlingHttpServletRequest req,
			final SlingHttpServletResponse resp) throws ServletException,
			IOException {
		final String guardianNewsAPI ="http://content.guardianapis.com/search?api-key=test&page-size=4&q="; 
		JSONArray completeJson = new JSONArray();
		JSONObject appleJson = readJsonFromUrl(guardianNewsAPI+"apple%20tech");
		JSONObject globeJson = readJsonFromUrl(guardianNewsAPI+"world");
		JSONObject googleJson = readJsonFromUrl(guardianNewsAPI+"google");
		JSONObject moneyJson = readJsonFromUrl(guardianNewsAPI+"money");

		completeJson.put(appleJson);
		completeJson.put(globeJson);
		completeJson.put(googleJson);
		completeJson.put(moneyJson);

		String[] topics = { "apple", "globe", "google", "money" };

		String html = "", active = "";
		String[] dataHtml = new String[4];

		for (int i = 0; i < 4; i++) {

			// Adding 'active' class to the html of first tab
			if (i == 0)	active = "active";
			else active = "";
			
			JSONObject x = completeJson.getJSONObject(i);
			JSONObject response = x.getJSONObject("response"); // Getting response object
			JSONArray res = new JSONArray();
			res = response.getJSONArray("results"); // Reading results

			
			// Generating HTML to be returned to client side
			dataHtml[i] = "<div role=\"tabpanel\" class=\"tab-pane " + active
					+ "\" id=\"" + topics[i] + "\">"
					+ "<ul class=\"list-group\">";
			for (int k = 0; k < res.length(); k++) {
				JSONObject p = res.getJSONObject(k);
				String data = "<li class=\"list-group-item\"><span class=\"glyphicon glyphicon-pushpin\">"
						+ "</span> <span class=\"label label-info\">"
						+ p.get("sectionId")
						+ "</span> "
						+ "<a target=\"_blank\" href = \""
						+ p.get("webUrl")
						+ "\"> "
						+ p.get("webTitle")
						+ "</a> </li>";

				dataHtml[i] = dataHtml[i] + data;
			}
		
			dataHtml[i] = dataHtml[i] + "</ul></div>";
			html = html + dataHtml[i];
		}
		resp.setContentType("text/html");
		resp.getWriter().write(html);
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException,
			JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}
}
