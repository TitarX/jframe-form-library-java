//Сохрание и восстановление параметров формы

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jframeformlibrary;

import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author TitarX
 */
public class FormPosition
{

    private JFrame form=null;
    private String xmlFilePath=null;

    public FormPosition(JFrame form,String xmlFilePath)
    {
        this.form=form;
        this.xmlFilePath=xmlFilePath;
    }

    //Сохрание параметров формы в xml-файл,
    //при закрытии формы или завершении работы приложения
    public void memorize() throws ParserConfigurationException,TransformerConfigurationException,TransformerException
    {
        File file=new File(xmlFilePath);
        if(!file.exists())
        {
            String pathString=file.getParent();
            if(pathString!=null)
            {
                File path=new File(pathString);
                path.mkdirs();
            }
        }
        Document configFileDocument=DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element configElement=configFileDocument.createElement("config");
        configFileDocument.appendChild(configElement);

        Element mainformElement=configFileDocument.createElement("mainform");
        configElement.appendChild(mainformElement);

        Element maximizedElement=configFileDocument.createElement("maximized");
        if(form.getExtendedState()==JFrame.MAXIMIZED_BOTH)
        {
            maximizedElement.appendChild(configFileDocument.createTextNode("1"));
        }
        else
        {
            maximizedElement.appendChild(configFileDocument.createTextNode("-1"));
        }
        mainformElement.appendChild(maximizedElement);

        Element sizeElement=configFileDocument.createElement("size");
        Element wElement=configFileDocument.createElement("w");
        wElement.appendChild(configFileDocument.createTextNode(String.valueOf((int)form.getSize().getWidth())));
        Element hElement=configFileDocument.createElement("h");
        hElement.appendChild(configFileDocument.createTextNode(String.valueOf((int)form.getSize().getHeight())));
        sizeElement.appendChild(wElement);
        sizeElement.appendChild(hElement);
        mainformElement.appendChild(sizeElement);

        Element locationElement=configFileDocument.createElement("location");
        Element xElement=configFileDocument.createElement("x");
        xElement.appendChild(configFileDocument.createTextNode(String.valueOf((int)form.getLocation().getX())));
        Element yElement=configFileDocument.createElement("y");
        yElement.appendChild(configFileDocument.createTextNode(String.valueOf((int)form.getLocation().getY())));
        locationElement.appendChild(xElement);
        locationElement.appendChild(yElement);
        mainformElement.appendChild(locationElement);

        Transformer configFileTransformer=TransformerFactory.newInstance().newTransformer();
        configFileTransformer.setOutputProperty(OutputKeys.INDENT,"yes");
        configFileTransformer.setOutputProperty(OutputKeys.METHOD,"xml");
        configFileTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
        DOMSource configFileDOMSource=new DOMSource(configFileDocument);
        StreamResult configFileStreamResult=new StreamResult(file);
        configFileTransformer.transform(configFileDOMSource,configFileStreamResult);
    }

    //Применение параметров формы из xml-файла
    public void restore() throws ParserConfigurationException,SAXException,IOException
    {
        File configFile=new File(xmlFilePath);
        if(configFile.exists())
        {
            Document configFileDocument=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configFile);
            Element rootElement=configFileDocument.getDocumentElement();
            NodeList mainformNodeList=rootElement.getElementsByTagName("mainform").item(0).getChildNodes();
            Node maximizedNode=getNodeByName(mainformNodeList,"maximized");
            String maximizedValue=null;
            if(maximizedNode!=null)
            {
                maximizedValue=maximizedNode.getTextContent().trim();
            }
            if(maximizedValue!=null&&maximizedValue.equals("1"))
            {
                form.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
            else
            {
                Node sizeNode=getNodeByName(mainformNodeList,"size");
                Node locationNode=getNodeByName(mainformNodeList,"location");

                if(sizeNode!=null&&locationNode!=null)
                {
                    Node xNode=getNodeByName(locationNode.getChildNodes(),"x");
                    Node yNode=getNodeByName(locationNode.getChildNodes(),"y");
                    Node wNode=getNodeByName(sizeNode.getChildNodes(),"w");
                    Node hNode=getNodeByName(sizeNode.getChildNodes(),"h");

                    if(xNode!=null&&yNode!=null&&wNode!=null&&hNode!=null)
                    {
                        String xValue=xNode.getTextContent().trim();
                        String yValue=yNode.getTextContent().trim();
                        String wValue=wNode.getTextContent().trim();
                        String hValue=hNode.getTextContent().trim();

                        String numberRegex="-?([1-9][0-9]*)|0";
                        String numberRegexWithoutZero="[1-9][0-9]*";
                        if(xValue.matches(numberRegex)&&yValue.matches(numberRegex)
                                &&wValue.matches(numberRegexWithoutZero)&&hValue.matches(numberRegexWithoutZero))
                        {
                            form.setSize(Integer.parseInt(wValue),Integer.parseInt(hValue));
                            form.setLocation(Integer.parseInt(xValue),Integer.parseInt(yValue));
                        }
                        else
                        {
                            form.setSize(800,600);
                            form.setLocationRelativeTo(null);
                        }
                    }
                    else
                    {
                        form.setSize(800,600);
                        form.setLocationRelativeTo(null);
                    }
                }
                else
                {
                    form.setSize(800,600);
                    form.setLocationRelativeTo(null);
                }
            }
        }
        else
        {
            form.setSize(800,600);
            form.setLocationRelativeTo(null);
        }
    }

    public void resetToDefault()
    {
        form.setSize(800,600);
        form.setLocationRelativeTo(null);
    }

    public void showErrorMessageDialog(Exception ex)
    {
        JOptionPane.showMessageDialog(form,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
    }

    private Node getNodeByName(NodeList nodeList,String nodeName)
    {
        for(int i=0;i<nodeList.getLength();i++)
        {
            Node node=nodeList.item(i);
            if(node.getNodeName().trim().equalsIgnoreCase(nodeName))
            {
                return node;
            }
        }
        return null;
    }
}
