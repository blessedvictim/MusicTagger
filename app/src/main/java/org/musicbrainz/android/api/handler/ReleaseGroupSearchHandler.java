package org.musicbrainz.android.api.handler;

import org.musicbrainz.android.api.data.ReleaseArtist;
import org.musicbrainz.android.api.data.ReleaseGroupInfo;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.LinkedList;

public class ReleaseGroupSearchHandler extends MBHandler {

    private LinkedList<ReleaseGroupInfo> releaseGroup = new LinkedList<ReleaseGroupInfo>();
    private ReleaseGroupInfo rg;
    private ReleaseArtist releaseArtist;

    private boolean inArtist;

    public LinkedList<ReleaseGroupInfo> getResults() {
        return releaseGroup;
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

        if (localName.equals("release-group")) {
            rg = new ReleaseGroupInfo();
            rg.setMbid(atts.getValue("id"));
            rg.setType(atts.getValue("type"));
        } else if (localName.equals("title")) {
            buildString();
        } else if (localName.equals("artist")) {
            inArtist = true;
            releaseArtist = new ReleaseArtist();
            releaseArtist.setMbid(atts.getValue("id"));
        } else if (localName.equals("name") && inArtist) {
            buildString();
        } else if (localName.equals("release")) {
            rg.addReleaseMbid(atts.getValue("id"));
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

        if (localName.equals("release-group")) {
            releaseGroup.add(rg);
        } else if (localName.equals("title")) {
            rg.setTitle(getString());
        } else if (localName.equals("artist")) {
            inArtist = false;
        } else if (localName.equals("name") && inArtist) {
            releaseArtist.setName(getString());
            rg.addArtist(releaseArtist);
        }
    }

}
