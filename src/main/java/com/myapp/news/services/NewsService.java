package com.myapp.news.services;

//import com.google.api.core.ApiFuture;
//import com.google.cloud.firestore.DocumentReference;
//import com.google.cloud.firestore.DocumentSnapshot;
//import com.google.cloud.firestore.Firestore;
//import com.google.cloud.firestore.WriteResult;
//import com.google.firebase.cloud.FirestoreClient;
import com.myapp.news.dtos.NewsArticle;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class NewsService {

    public String createContent(NewsArticle newsArticle) throws ExecutionException, InterruptedException {
        /*Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("newsArticles").document(newsContent.getContent()).set(newsContent);
        return collectionsApiFuture.get().getUpdateTime().toString();*/
        return "";

    }

    public NewsArticle getNewsById(String newsId) throws ExecutionException, InterruptedException {
        /*Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("newsArticles").document(newsId);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        NewsContent newsContent;
        if(document.exists()){
            newsContent = document.toObject(NewsContent.class);
            return newsContent;
        }*/
        return null;
    }

    public String updateContent(NewsArticle newsArticle) {

        return "";
    }

    public String deleteNewsById(String newsId) {
        /*Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection("newsArticles").document(newsId).delete();
        */return "Successfully deleted " + newsId;
    }
}
