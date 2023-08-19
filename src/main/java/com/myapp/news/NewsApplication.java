package com.myapp.news;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@SpringBootApplication
public class NewsApplication {

	// For Heroku
	public static void main(String[] args) {
		SpringApplication.run(NewsApplication.class, args);
	}

	// for running the app on Firebase - uncomment the below block
	/*public static void main(String[] args) throws IOException {
		ClassLoader classLoader = NewsApplication.class.getClassLoader();

		File file = new File(Objects.requireNonNull(classLoader.getResource("serviceAccountKey.json")).getFile());
		FileInputStream serviceAccount = new FileInputStream(file.getAbsolutePath());

		FirebaseOptions options = new FirebaseOptions.Builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount))
				.setDatabaseUrl("https://news-feed-bc994-default-rtdb.firebaseio.com")
				.build();

		boolean hasBeenInitialized=false;
		List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
		FirebaseApp finestayApp;
		for(FirebaseApp app : firebaseApps){
			if(app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)){
				hasBeenInitialized=true;
				finestayApp = app;
			}
		}

		if(!hasBeenInitialized) {
			finestayApp = FirebaseApp.initializeApp(options);
		}

		SpringApplication.run(NewsApplication.class, args);
	}
	**/

}
