/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.jaxen;

import eu.maveniverse.domtrip.Attribute;
import eu.maveniverse.domtrip.Comment;
import eu.maveniverse.domtrip.ContainerNode;
import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.Element;
import eu.maveniverse.domtrip.NamespaceContext;
import eu.maveniverse.domtrip.NamespaceResolver;
import eu.maveniverse.domtrip.Node;
import eu.maveniverse.domtrip.ProcessingInstruction;
import eu.maveniverse.domtrip.Text;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jaxen.DefaultNavigator;
import org.jaxen.FunctionCallException;
import org.jaxen.JaxenConstants;
import org.jaxen.Navigator;
import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;

/**
 * Jaxen {@link Navigator} implementation for the DomTrip XML object model.
 *
 * <p>This navigator enables full XPath 1.0 evaluation against DomTrip's lossless
 * XML tree. It maps DomTrip's node types ({@link Element}, {@link Text},
 * {@link Comment}, {@link ProcessingInstruction}, {@link Document}) to Jaxen's
 * expected navigation model.</p>
 *
 * <p>Since DomTrip's {@link Attribute} class does not extend {@link Node} and does
 * not store a parent reference, this navigator uses {@link AttributeNode} wrappers
 * on the attribute axis. Similarly, {@link NamespaceNode} wrappers represent
 * namespace declarations on the namespace axis.</p>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * Navigator nav = DomTripNavigator.getInstance();
 * DomTripXPath xpath = new DomTripXPath("//dependency[@scope='test']");
 * List results = xpath.selectNodes(root);
 * }</pre>
 *
 * @since 1.3.0
 * @see DomTripXPath
 * @see eu.maveniverse.domtrip.jaxen.XPath
 */
@SuppressWarnings("java:S6548") // Singleton is intentional for Jaxen's Navigator pattern
public class DomTripNavigator extends DefaultNavigator {

    private static final long serialVersionUID = 1L;

    private static final DomTripNavigator INSTANCE = new DomTripNavigator();

    /**
     * Returns the singleton navigator instance.
     *
     * @return the shared {@code DomTripNavigator}
     */
    public static Navigator getInstance() {
        return INSTANCE;
    }

    // ---- Child axis ----

    @Override
    @SuppressWarnings("rawtypes")
    public Iterator getChildAxisIterator(Object contextNode) {
        if (contextNode instanceof ContainerNode) {
            List<Object> children = new ArrayList<>();
            ((ContainerNode) contextNode).children().forEach(children::add);
            return children.iterator();
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    // ---- Parent axis ----

    @Override
    @SuppressWarnings("rawtypes")
    public Iterator getParentAxisIterator(Object contextNode) {
        Object parent = getParentNode(contextNode);
        if (parent != null) {
            return Collections.singletonList(parent).iterator();
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    @Override
    public Object getParentNode(Object contextNode) {
        if (contextNode instanceof Node) {
            return ((Node) contextNode).parent();
        }
        if (contextNode instanceof AttributeNode) {
            return ((AttributeNode) contextNode).element();
        }
        if (contextNode instanceof NamespaceNode) {
            return ((NamespaceNode) contextNode).element();
        }
        return null;
    }

    // ---- Sibling axes ----

    @Override
    @SuppressWarnings("rawtypes")
    public Iterator getFollowingSiblingAxisIterator(Object contextNode) {
        if (contextNode instanceof Node) {
            List<Object> siblings = new ArrayList<>();
            Optional<Node> next = ((Node) contextNode).nextSibling();
            while (next.isPresent()) {
                siblings.add(next.get());
                next = next.get().nextSibling();
            }
            return siblings.iterator();
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Iterator getPrecedingSiblingAxisIterator(Object contextNode) {
        if (contextNode instanceof Node) {
            List<Object> siblings = new ArrayList<>();
            Optional<Node> prev = ((Node) contextNode).previousSibling();
            while (prev.isPresent()) {
                siblings.add(prev.get());
                prev = prev.get().previousSibling();
            }
            return siblings.iterator();
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    // ---- Attribute axis ----

    @Override
    @SuppressWarnings("rawtypes")
    public Iterator getAttributeAxisIterator(Object contextNode) {
        if (contextNode instanceof Element) {
            Element element = (Element) contextNode;
            List<Object> attributes = new ArrayList<>();
            for (Map.Entry<String, Attribute> entry : element.attributeObjects().entrySet()) {
                String name = entry.getKey();
                if (!isNamespaceDeclaration(name)) {
                    attributes.add(new AttributeNode(element, name, entry.getValue()));
                }
            }
            return attributes.iterator();
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    // ---- Namespace axis ----

    @Override
    @SuppressWarnings("rawtypes")
    public Iterator getNamespaceAxisIterator(Object contextNode) {
        if (contextNode instanceof Element) {
            Element element = (Element) contextNode;
            NamespaceContext ctx = element.namespaceContext();
            List<Object> namespaces = new ArrayList<>();
            // Always include the xml namespace
            namespaces.add(new NamespaceNode(element, "xml", "http://www.w3.org/XML/1998/namespace"));
            // Default namespace
            String defaultUri = ctx.defaultNamespaceURI();
            if (defaultUri != null && !defaultUri.isEmpty()) {
                namespaces.add(new NamespaceNode(element, "", defaultUri));
            }
            // Prefixed namespaces
            for (String prefix : ctx.declaredPrefixes()) {
                String uri = ctx.namespaceURI(prefix);
                if (uri != null) {
                    namespaces.add(new NamespaceNode(element, prefix, uri));
                }
            }
            return namespaces.iterator();
        }
        return JaxenConstants.EMPTY_ITERATOR;
    }

    // ---- Document ----

    @Override
    public Object getDocumentNode(Object contextNode) {
        if (contextNode instanceof Document) {
            return contextNode;
        }
        if (contextNode instanceof Node) {
            return ((Node) contextNode).document();
        }
        if (contextNode instanceof AttributeNode) {
            return ((AttributeNode) contextNode).element().document();
        }
        if (contextNode instanceof NamespaceNode) {
            return ((NamespaceNode) contextNode).element().document();
        }
        return null;
    }

    // ---- Node type checks ----

    @Override
    public boolean isDocument(Object object) {
        return object instanceof Document;
    }

    @Override
    public boolean isElement(Object object) {
        return object instanceof Element;
    }

    @Override
    public boolean isAttribute(Object object) {
        return object instanceof AttributeNode;
    }

    @Override
    public boolean isText(Object object) {
        return object instanceof Text;
    }

    @Override
    public boolean isComment(Object object) {
        return object instanceof Comment;
    }

    @Override
    public boolean isProcessingInstruction(Object object) {
        return object instanceof ProcessingInstruction;
    }

    @Override
    public boolean isNamespace(Object object) {
        return object instanceof NamespaceNode;
    }

    // ---- Element accessors ----

    @Override
    public String getElementName(Object element) {
        return ((Element) element).localName();
    }

    @Override
    public String getElementQName(Object element) {
        return ((Element) element).name();
    }

    @Override
    public String getElementNamespaceUri(Object element) {
        String uri = ((Element) element).namespaceURI();
        return uri != null ? uri : "";
    }

    @Override
    public String getElementStringValue(Object element) {
        return getDeepTextContent((Element) element);
    }

    // ---- Attribute accessors ----

    @Override
    public String getAttributeName(Object attr) {
        String name = ((AttributeNode) attr).name();
        int colon = name.indexOf(':');
        return colon >= 0 ? name.substring(colon + 1) : name;
    }

    @Override
    public String getAttributeQName(Object attr) {
        return ((AttributeNode) attr).name();
    }

    @Override
    public String getAttributeNamespaceUri(Object attr) {
        AttributeNode an = (AttributeNode) attr;
        String name = an.name();
        int colon = name.indexOf(':');
        if (colon >= 0) {
            String prefix = name.substring(0, colon);
            String uri = NamespaceResolver.resolveNamespaceURI(an.element(), prefix);
            return uri != null ? uri : "";
        }
        return "";
    }

    @Override
    public String getAttributeStringValue(Object attr) {
        return ((AttributeNode) attr).value();
    }

    // ---- Text accessors ----

    @Override
    public String getTextStringValue(Object text) {
        return ((Text) text).content();
    }

    // ---- Comment accessors ----

    @Override
    public String getCommentStringValue(Object comment) {
        return ((Comment) comment).content();
    }

    // ---- Processing instruction accessors ----

    @Override
    public String getProcessingInstructionTarget(Object pi) {
        return ((ProcessingInstruction) pi).target();
    }

    @Override
    public String getProcessingInstructionData(Object pi) {
        return ((ProcessingInstruction) pi).data();
    }

    // ---- Namespace accessors ----

    @Override
    public String getNamespacePrefix(Object ns) {
        return ((NamespaceNode) ns).prefix();
    }

    @Override
    public String getNamespaceStringValue(Object ns) {
        return ((NamespaceNode) ns).uri();
    }

    // ---- Namespace resolution ----

    @Override
    public String translateNamespacePrefixToUri(String prefix, Object context) {
        Element element = resolveElement(context);
        if (element != null) {
            return NamespaceResolver.resolveNamespaceURI(element, prefix);
        }
        return null;
    }

    // ---- XPath creation ----

    @Override
    public XPath parseXPath(String xpath) throws SAXPathException {
        return new DomTripXPath(xpath);
    }

    // ---- Unsupported operations ----

    @Override
    public Object getDocument(String uri) throws FunctionCallException {
        return null;
    }

    @Override
    public Object getElementById(Object contextNode, String elementId) {
        return null;
    }

    // ---- Private helpers ----

    private static boolean isNamespaceDeclaration(String attrName) {
        return "xmlns".equals(attrName) || attrName.startsWith("xmlns:");
    }

    private static Element resolveElement(Object context) {
        if (context instanceof Element) {
            return (Element) context;
        }
        if (context instanceof Node) {
            return ((Node) context).parentElement();
        }
        if (context instanceof AttributeNode) {
            return ((AttributeNode) context).element();
        }
        if (context instanceof NamespaceNode) {
            return ((NamespaceNode) context).element();
        }
        return null;
    }

    private static String getDeepTextContent(ContainerNode container) {
        StringBuilder sb = new StringBuilder();
        collectTextContent(container, sb);
        return sb.toString();
    }

    private static void collectTextContent(ContainerNode container, StringBuilder sb) {
        container.children().forEach(child -> {
            if (child instanceof Text) {
                sb.append(((Text) child).content());
            } else if (child instanceof ContainerNode) {
                collectTextContent((ContainerNode) child, sb);
            }
        });
    }
}
