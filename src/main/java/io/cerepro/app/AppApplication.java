package io.cerepro.app;

import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1beta2.LanguageServiceSettings;
import com.monkeylearn.MonkeyLearn;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class AppApplication {

	private MonkeyLearn monkeyLearn = new MonkeyLearn("3a1676bd86dc12eb834f90d160522b799ade3d18");

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

	public MonkeyLearn getMonkeyLearn() {
		return monkeyLearn;
	}

	//public LanguageServiceClient getLanguageServiceClient() {
	//	try {
	//		return LanguageServiceClient.create();
	//	} catch (IOException e) {
	//		e.printStackTrace();
	//	}
	//	return null;
	//}
}
