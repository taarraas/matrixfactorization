/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knu.univ.lingvo.matrixfactorization;

import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author taras
 */
public class WikiPageHandler extends DefaultHandler {
    Logger log = Logger.getLogger("WikiPageHandler");
    PageHandler innerHandler;
    boolean isInText = false;
    boolean isInTitle = false;
    int count = 0;
    StringBuffer currentPage = new StringBuffer();
    StringBuffer currentTitle = new StringBuffer();
    

    public WikiPageHandler(PageHandler innerHandler) {
        this.innerHandler = innerHandler;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.compareToIgnoreCase("text") == 0)
        {
            currentPage = new StringBuffer();
            isInText = true;
        } else if (qName.compareToIgnoreCase("title") == 0)
        {
            currentTitle = new StringBuffer();
            isInTitle = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.compareToIgnoreCase("text") == 0)
        {            
           innerHandler.handle(currentPage.toString());
           isInText = false;
        } else if (qName.compareToIgnoreCase("title") == 0)
        {
            count++;
            log.info("New page # " + count + " : " + currentTitle.toString());
            isInTitle = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (isInText)
            currentPage.append(ch, start, length);
        else if (isInTitle)
            currentTitle.append(ch, start, length);
    }
    
    
    
}
