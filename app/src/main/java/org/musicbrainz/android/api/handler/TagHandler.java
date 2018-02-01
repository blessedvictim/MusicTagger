package org.musicbrainz.android.api.handler;

import org.musicbrainz.android.api.data.Tag;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.LinkedList;

public class TagHandler extends MBHandler {

    private LinkedList<Tag> tags = new LinkedList<Tag>();
    private Tag tag;

    public LinkedList<Tag> getTags() {
        return tags;
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (localName.equals("tag")) {
            buildString();
            tag = new Tag();
            tag.setCount(Integer.parseInt(atts.getValue("count")));
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

        if (localName.equals("tag")) {
            tag.setText(getString());
            tags.add(tag);
        }
    }

}
