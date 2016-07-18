package scenarii.camera;
//
//  GifSequenceWriter.java
//
//  Created by Elliot Kroo on 2009-04-25.
//
// This work is licensed under the Creative Commons Attribution 3.0 Unported
// License. To view a copy of this license, visit
// http://creativecommons.org/licenses/by/3.0/ or send a letter to Creative
// Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.


import org.w3c.dom.Node;

import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Iterator;
import java.util.function.Consumer;


/**
 * Created by Elliot Kroo on 2009-04-25.
 * Addapted by Geoffrey Gaillard, from Elliot Kroo's awesome work.
 * Please see the credits in the source file. (CC-BY)
 * (elliot[at]kroo[dot]net)
 */
class GifSequenceWriter {

    private ImageWriter gifWriter;
    private ImageWriteParam imageWriteParam;
    private IIOMetadata imageMetaData;


    /**
     * Creates a new GifSequenceWriter
     *
     * @param outputStream        the ImageOutputStream to be written to
     * @param imageType           one of the imageTypes specified in BufferedImage
     * @param timeBetweenFramesMS the time between frames in miliseconds
     * @param loopContinuously    wether the gif should loop repeatedly
     * @throws IIOException if no gif ImageWriters are found
     * @author Elliot Kroo (elliot[at]kroo[dot]net)
     */
    public GifSequenceWriter(
            ImageOutputStream outputStream,
            int imageType,
            double timeBetweenFramesMS,
            boolean loopContinuously) throws IOException {
        // my method to create a writer
        gifWriter = getWriter();
        imageWriteParam = gifWriter.getDefaultWriteParam();
        ImageTypeSpecifier imageTypeSpecifier =
                ImageTypeSpecifier.createFromBufferedImageType(imageType);

        imageMetaData =
                gifWriter.getDefaultImageMetadata(imageTypeSpecifier,
                        imageWriteParam);

        String metaFormatName = imageMetaData.getNativeMetadataFormatName();

        IIOMetadataNode root = (IIOMetadataNode)
                imageMetaData.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(
                root,
                "GraphicControlExtension");

        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute(
                "transparentColorFlag",
                "FALSE");
        System.out.println("timeBetweenFramesMS: " + (timeBetweenFramesMS / 10));
        graphicsControlExtensionNode.setAttribute(
                "delayTime",
                Integer.toString((int)(timeBetweenFramesMS/10)));
        graphicsControlExtensionNode.setAttribute(
                "transparentColorIndex",
                "0");

        IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
        commentsNode.setAttribute("CommentExtension", "Created by MAH");

        IIOMetadataNode appEntensionsNode = getNode(
                root,
                "ApplicationExtensions");

        IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");

        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");

        int loop = loopContinuously ? 0 : 1;

        child.setUserObject(new byte[]{0x1, (byte) (loop & 0xFF), (byte)
                ((loop >> 8) & 0xFF)});
        appEntensionsNode.appendChild(child);

        imageMetaData.setFromTree(metaFormatName, root);

        gifWriter.setOutput(outputStream);

        gifWriter.prepareWriteSequence(null);
    }

    public void writeToSequence(RenderedImage img) throws IOException {
        gifWriter.writeToSequence(
                new IIOImage(
                        img,
                        null,
                        imageMetaData),
                imageWriteParam);
    }

    /**
     * Close this GifSequenceWriter object. This does not close the underlying
     * stream, just finishes off the GIF.
     */
    public void close() throws IOException {
        gifWriter.endWriteSequence();
    }

    /**
     * Returns the first available GIF ImageWriter using
     * ImageIO.getImageWritersBySuffix("gif").
     *
     * @return a GIF ImageWriter object
     * @throws IIOException if no GIF image writers are returned
     */
    private static ImageWriter getWriter() throws IIOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
        if (!iter.hasNext()) {
            throw new IIOException("No GIF Image Writers Exist");
        } else {
            return iter.next();
        }
    }

    /**
     * Returns an existing child node, or creates and returns a new child node (if
     * the requested node does not exist).
     *
     * @param rootNode the <tt>IIOMetadataNode</tt> to search for the child node.
     * @param nodeName the name of the child node.
     * @return the child node, if found or a new node created with the given name.
     */
    private static IIOMetadataNode getNode(
            IIOMetadataNode rootNode,
            String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName)
                    == 0) {
                return ((IIOMetadataNode) rootNode.item(i));
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return (node);
    }

    public void fixFrameRate(String path, long time, Consumer<String> callback){
        try {
            File file = new File(path);
            File tempFile = new File(path + ".temp");
            // Get GIF reader
            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            // Give it the stream to decode from
            reader.setInput(ImageIO.createImageInputStream(file));
            int numImages = reader.getNumImages(true);

            ImageOutputStream ios = new FileImageOutputStream(tempFile);
            // Get GIF writer that's compatible with reader
            GifSequenceWriter writer = new GifSequenceWriter(
                    ios,
                    BufferedImage.TYPE_INT_RGB,
                    time/numImages,
                    true);
            // Give it the stream to encode to

            System.out.println("NumImages: "+numImages);
            System.out.println("Delta: "+ time/numImages);
            for (int i = 0; i < numImages; i++) {
                // Get input image
                BufferedImage frameIn = reader.read(i);
                writer.writeToSequence(frameIn);
            }
            writer.close();
            file.delete();
            tempFile.renameTo(file);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            callback.accept(path);
        }
        catch (IOException e) {
            System.err.println("ERROR: Unable to read generated GIF file to fix... suspicious ...");
            System.err.println("       (GifSequenceWriter::fixFrameRate) ==>");
            System.err.println(e.getMessage());
        }
    }
}