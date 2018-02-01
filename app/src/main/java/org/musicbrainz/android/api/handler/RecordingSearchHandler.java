package org.musicbrainz.android.api.handler;

import org.musicbrainz.android.api.data.RecordingInfo;
import org.musicbrainz.android.api.data.ReleaseArtist;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.LinkedList;

public class RecordingSearchHandler extends MBHandler {

    private LinkedList<RecordingInfo> results = new LinkedList<RecordingInfo>();
    private RecordingInfo recording;
    private ReleaseArtist recordingArtist;

    private boolean inReleaseList;
    private boolean inArtist;

    public LinkedList<RecordingInfo> getResults() {
        return results;
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

        if (localName.equals("recording")) {
            recording = new RecordingInfo();
            recording.setMbid(atts.getValue("id"));
        } else if (localName.equals("title") && !inReleaseList) {
            buildString();
        } else if (localName.equals("artist")) {
            inArtist = true;
            recordingArtist = new ReleaseArtist();
            recordingArtist.setMbid(atts.getValue("id"));
        } else if (localName.equals("name") && inArtist) {
            buildString();
        } else if (localName.equals("length")) {
            buildString();
        } else if (localName.equals("release-list")) {
            inReleaseList = true;
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

        if (localName.equals("recording")) {
            results.add(recording);
        } else if (localName.equals("title") && !inReleaseList) {
            recording.setTitle(getString());
        } else if (localName.equals("artist")) {
            inArtist = false;
            recording.setArtist(recordingArtist);
        } else if (localName.equals("name") && inArtist) {
            recordingArtist.setName(getString());
        } else if (localName.equals("length")) {
            recording.setLength(Integer.parseInt(getString()));
        } else if (localName.equals("release-list")) {
            inReleaseList = false;
        }
    }

}
