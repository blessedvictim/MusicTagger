package org.musicbrainz.android.api.webservice;

import org.musicbrainz.android.api.MusicBrainz;
import org.musicbrainz.android.api.data.Artist;
import org.musicbrainz.android.api.data.ArtistSearchResult;
import org.musicbrainz.android.api.data.Label;
import org.musicbrainz.android.api.data.LabelSearchResult;
import org.musicbrainz.android.api.data.Recording;
import org.musicbrainz.android.api.data.RecordingInfo;
import org.musicbrainz.android.api.data.Release;
import org.musicbrainz.android.api.data.ReleaseGroup;
import org.musicbrainz.android.api.data.ReleaseGroupInfo;
import org.musicbrainz.android.api.data.ReleaseInfo;
import org.musicbrainz.android.api.data.Tag;
import org.musicbrainz.android.api.data.UserCollection;
import org.musicbrainz.android.api.data.UserCollectionInfo;
import org.musicbrainz.android.api.data.UserData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Makes the web service available for Activity classes. Calls are blocking and
 * should be made asynchronously. The XML returned is parsed into pojos with
 * SAX handlers.
 */
public class MusicBrainzWebClient implements MusicBrainz {

    private static final String AUTH_REALM = "musicbrainz.org";
    private static final String AUTH_SCOPE = "musicbrainz.org";
    private static final int AUTH_PORT = 80;
    private static final String AUTH_TYPE = "Digest";

    OkHttpClient client;
    private ResponseParser responseParser;
    private String clientId;

    public MusicBrainzWebClient(String userAgent) {
        //httpClient = HttpClient.getClient(userAgent);

        client = new OkHttpClient();

        responseParser = new ResponseParser();
    }

    /*public MusicBrainzWebClient(User user, String userAgent, String clientId) {
        httpClient = HttpClient.getClient(userAgent);
        responseParser = new ResponseParser();
        setCredentials(user.getUsername(), user.getPassword());
        this.clientId = clientId;
    }

    @Override
    public void setCredentials(String username, String password) {
        AuthScope authScope = new AuthScope(AUTH_SCOPE, AUTH_PORT, AUTH_REALM, AUTH_TYPE);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        httpClient.getCredentialsProvider().setCredentials(authScope, credentials);
    }*/

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Release lookupReleaseUsingBarcode(String barcode) throws IOException {
        ResponseBody entity = get(QueryBuilder.barcodeSearch(barcode));
        String barcodeMbid = responseParser.parseMbidFromBarcode(entity.byteStream());
        entity.close();
        if (barcodeMbid == null) {
            throw new BarcodeNotFoundException(barcode);
        }
        return lookupRelease(barcodeMbid);
    }

    @Override
    public Release lookupRelease(String mbid) throws IOException {
        ResponseBody entity = get(QueryBuilder.releaseLookup(mbid));
        Release release = responseParser.parseRelease(entity.byteStream());
        entity.close();
        return release;
    }

    @Override
    public LinkedList<ReleaseInfo> browseReleases(String mbid) throws IOException {
        ResponseBody entity = get(QueryBuilder.releaseGroupReleaseBrowse(mbid));
        LinkedList<ReleaseInfo> releases = responseParser.parseReleaseGroupReleases(entity.byteStream());
        entity.close();
        Collections.sort(releases);
        return releases;
    }

    @Override
    public Artist lookupArtist(String mbid) throws IOException {
        ResponseBody entity = get(QueryBuilder.artistLookup(mbid));
        Artist artist = responseParser.parseArtist(entity.byteStream());
        entity.close();
        artist.setReleaseGroups(browseArtistReleaseGroups(mbid));
        return artist;
    }

    private ArrayList<ReleaseGroupInfo> browseArtistReleaseGroups(String mbid) throws IOException {
        ResponseBody entity = get(QueryBuilder.artistReleaseGroupBrowse(mbid, 0));
        ArrayList<ReleaseGroupInfo> releases = responseParser.parseReleaseGroupBrowse(entity.byteStream());
        entity.close();
        Collections.sort(releases);
        return releases;
    }

    @Override
    public ReleaseGroup lookupReleaseGroup(String mbid) throws IOException {
        ResponseBody entity = get(QueryBuilder.releaseGroupLookup(mbid));
        ReleaseGroup rg = responseParser.parseReleaseGroupLookup(entity.byteStream());
        entity.close();
        return rg;
    }

    @Override
    public Label lookupLabel(String mbid) throws IOException {
        ResponseBody entity = get(QueryBuilder.labelLookup(mbid));
        Label label = responseParser.parseLabel(entity.byteStream());
        entity.close();
        return label;
    }

    @Override
    public Recording lookupRecording(String mbid) throws IOException {
        ResponseBody entity = get(QueryBuilder.recordingLookup(mbid));
        Recording recording = responseParser.parseRecording(entity.byteStream());
        entity.close();
        return recording;
    }

    @Override
    public LinkedList<ArtistSearchResult> searchArtist(String searchTerm) throws IOException {
        ResponseBody entity = get(QueryBuilder.artistSearch(searchTerm));
        LinkedList<ArtistSearchResult> artists = responseParser.parseArtistSearch(entity.byteStream());
        entity.close();
        return artists;
    }

    @Override
    public LinkedList<ReleaseGroupInfo> searchReleaseGroup(String searchTerm) throws IOException {
        ResponseBody entity = get(QueryBuilder.releaseGroupSearch(searchTerm));
        LinkedList<ReleaseGroupInfo> releaseGroups = responseParser.parseReleaseGroupSearch(entity.byteStream());
        entity.close();
        return releaseGroups;
    }

    @Override
    public LinkedList<ReleaseInfo> searchRelease(String searchTerm) throws IOException {
        ResponseBody entity = get(QueryBuilder.releaseSearch(searchTerm));
        LinkedList<ReleaseInfo> releases = responseParser.parseReleaseSearch(entity.byteStream());
        entity.close();
        return releases;
    }

    @Override
    public LinkedList<LabelSearchResult> searchLabel(String searchTerm) throws IOException {
        ResponseBody entity = get(QueryBuilder.labelSearch(searchTerm));
        LinkedList<LabelSearchResult> labels = responseParser.parseLabelSearch(entity.byteStream());
        entity.close();
        return labels;
    }

    @Override
    public LinkedList<RecordingInfo> searchRecording(String searchTerm) throws IOException {
        ResponseBody entity = get(QueryBuilder.recordingSearch(searchTerm));
        LinkedList<RecordingInfo> recordings = responseParser.parseRecordingSearch(entity.byteStream());
        entity.close();
        return recordings;
    }

    @Override
    public LinkedList<Tag> lookupTags(Entity type, String mbid) throws IOException {
        ResponseBody entity = get(QueryBuilder.tagLookup(type, mbid));
        LinkedList<Tag> tags = responseParser.parseTagLookup(entity.byteStream());
        entity.close();
        Collections.sort(tags);
        return tags;
    }

    @Override
    public float lookupRating(Entity type, String mbid) throws IOException {
        ResponseBody entity = get(QueryBuilder.ratingLookup(type, mbid));
        float rating = responseParser.parseRatingLookup(entity.byteStream());
        entity.close();
        return rating;
    }

    /*@Override
    public boolean autenticateCredentials() throws IOException {
        HttpGet authenticationTest = new HttpGet(QueryBuilder.authenticationCheck());
        authenticationTest.setHeader("Accept", "application/xml");
        try {
            httpClient.execute(authenticationTest, new BasicResponseHandler());
        } catch (HttpResponseException e) {
            return false;
        }
        return true;
    }*/

    @Override
    public UserData lookupUserData(Entity entityType, String mbid) throws IOException {
        ResponseBody entity = get(QueryBuilder.userData(entityType, mbid));
        UserData userData = responseParser.parseUserData(entity.byteStream());
        entity.close();
        return userData;
    }


    /*@Override
    public void addReleaseToCollection(String collectionMbid, String releaseMbid) throws IOException {
        put(QueryBuilder.collectionEdit(collectionMbid, releaseMbid, clientId));
    }*/

    /*@Override
    public void deleteReleaseFromCollection(String collectionMbid, String releaseMbid) throws IOException {
        delete(QueryBuilder.collectionEdit(collectionMbid, releaseMbid, clientId));
    }*/

    @Override
    public LinkedList<UserCollectionInfo> lookupUserCollections() throws IOException {
        ResponseBody entity = get(QueryBuilder.collectionList());
        LinkedList<UserCollectionInfo> collections = responseParser.parseCollectionListLookup(entity.byteStream());
        entity.close();
        Collections.sort(collections);
        return collections;
    }

    @Override
    public UserCollection lookupCollection(String mbid) throws IOException {
        ResponseBody entity = get(QueryBuilder.collectionLookup(mbid));
        UserCollection collection = responseParser.parseCollectionLookup(entity.byteStream());
        entity.close();
        return collection;
    }

    /*private HttpEntity get(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        get.setHeader("Accept", "application/xml");
        HttpResponse response = httpClient.execute(get);
        return response.getEntity();
    }*/

    ResponseBody get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/xml")
                .build();

        Response response = client.newCall(request).execute();
        return response.body();
    }

    /*private void post(String url, String content) throws IOException {
        HttpPost post = new HttpPost(url);
        post.addHeader("Content-Type", "application/xml; charset=UTF-8");
        StringEntity xml = new StringEntity(content, "UTF-8");
        post.setEntity(xml);
        HttpResponse response = httpClient.execute(post);
        response.getEntity().consumeContent();
    }


    private void delete(String url) throws IOException {
        HttpDelete delete = new HttpDelete(url);
        HttpResponse response = httpClient.execute(delete);
        response.getEntity().consumeContent();
    }

    private void put(String url) throws IOException {
        HttpPut put = new HttpPut(url);
        HttpResponse response = httpClient.execute(put);
        response.getEntity().consumeContent();
    }*/

}
