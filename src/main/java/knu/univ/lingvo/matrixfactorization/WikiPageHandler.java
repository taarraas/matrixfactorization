/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.matrixfactorization;

import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author taras
 */
public class WikiPageHandler extends DefaultHandler {
    PageHandler innerHandler;
    StringBuffer currentPage = new StringBuffer();


    public WikiPageHandler(PageHandler innerHandler) {
        this.innerHandler = innerHandler;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("text"))
        {
            currentPage = new StringBuffer();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("text"))
        {            
           innerHandler.handle(currentPage.toString());
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentPage.append(ch, start, length);
    }
    
    
    
}
