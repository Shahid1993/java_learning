package net.pdfix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.nio.file.SensitivityWatchEventModifier;

import net.pdfix.samples.AddTags;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class RecursiveDirectoryWatcher {

	private static File rootFolder;
	private static WatchService watcher;
	private static ExecutorService executor;

	public void init() throws Exception {
	    //rootFolder = new File("/home/codemantra/Desktop/Autotagging/input");
		rootFolder = new File("/home/codemantra/Documents/OAT/RD/PDFixSDKSample-java-master/taggedWithoutHeadings");		
		watcher = FileSystems.getDefault().newWatchService();
		executor = Executors.newSingleThreadExecutor();
	}

	public static void main(String[] args) {
		try {
			new RecursiveDirectoryWatcher().init();
			System.out.println("Starting Recursive Watcher");

			final Map<WatchKey, Path> keys = new HashMap<>();

			Consumer<Path> register = p -> {
				if (!p.toFile().exists() || !p.toFile().isDirectory()) {
					throw new RuntimeException("folder " + p + " does not exist or is not a directory");
				}
				try {
					Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
								throws IOException {
							System.out.println("registering " + dir + " in watcher service");
							WatchKey watchKey = dir.register(watcher, new WatchEvent.Kind[] { ENTRY_CREATE },
									SensitivityWatchEventModifier.HIGH);
							keys.put(watchKey, dir);
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					throw new RuntimeException("Error registering path " + p);
				}
			};

			register.accept(rootFolder.toPath());

			executor.submit(() -> {
				while (true) {
					final WatchKey key;
					try {
						key = watcher.take(); // wait for a key to be available
					} catch (InterruptedException ex) {
						return;
					}

					final Path dir = keys.get(key);
					if (dir == null) {
						System.err.println("WatchKey " + key + " not recognized!");
						continue;
					}

					key.pollEvents().stream().filter(e -> (e.kind() != OVERFLOW))
							.map(e -> ((WatchEvent<Path>) e).context()).forEach(p -> {
								final Path absPath = dir.resolve(p);
								if (absPath.toFile().isDirectory()) {
									register.accept(absPath);
								} else {
									final File f = absPath.toFile();
									System.out.println("Detected new file " + f.getAbsolutePath());

									if (FilenameUtils.getExtension(f.getName()).equalsIgnoreCase("pdf")) {
										try {
											while(!isCompletelyWritten(f)) {
												/**Waiting for file to be completey written **/
											}
										}catch(Exception e) {
											//DO nothing
										}
										autoTagFile(f);
										System.out.println("----------------------------------");
									}
								}
							});

					boolean valid = key.reset(); // IMPORTANT: The key must be reset after processed
					if (!valid) {
						break;
					}
				}
			});

		} catch (Exception e) {
			System.out.println("Exception ........");
			e.printStackTrace();
		}
	}

	public static void autoTagFile(File file) {

		try {
			System.out.println("Inside autotag file...");

			String baseNameWithPath = file.getParent() + File.separator + FilenameUtils.getBaseName(file.getName());
			String configPath = baseNameWithPath + ".json";

			if (!new File(configPath).exists()) {
				// Check if xml exists
				if (new File(baseNameWithPath + ".xml").exists()
						|| new File(baseNameWithPath + ".xmloutput.xml").exists()) {
					String xmlPath = new File(baseNameWithPath + ".xml").exists() ? baseNameWithPath + ".xml"
							: baseNameWithPath + ".xmloutput.xml";

					/** generate config file **/

					configPath = generateConfigFile(xmlPath, baseNameWithPath);

				} else {
					configPath = Utils.getAbsolutePath("resources/config_default.json");
				}
			}

			String outputFilePath = file.getAbsolutePath().replace("/taggedWithoutHeadings/", "/taggedWithHeadings/");

			addTags(file.getAbsolutePath(), outputFilePath, configPath);

		} catch (Exception e) {
			System.out.println("File not tagged...");
		}

	}
	
	private static boolean isCompletelyWritten(File file) throws InterruptedException{
	    Long fileSizeBefore = file.length();
	    Thread.sleep(3000);
	    Long fileSizeAfter = file.length();

	    System.out.println("comparing file size " + fileSizeBefore + " with " + fileSizeAfter);

	    if (fileSizeBefore.equals(fileSizeAfter)) {
	        return true;
	    }
	    return false;
	}

	public static String generateConfigFile(String xmlPath, String baseNameWithPath) {
		String configPath = null;

		System.out.println("Inside generateConfigFile....");

		try {
			File xmlFile = new File(xmlPath);
			System.out.println(xmlFile.getName());
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);

			XPathFactory xpathFactory = XPathFactory.newInstance();
			/** XPath to find empty text nodes. **/
			XPathExpression xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");
			NodeList emptyTextNodes = (NodeList) xpathExp.evaluate(doc, XPathConstants.NODESET);

			/** Remove each empty text node from document. **/
			for (int i = 0; i < emptyTextNodes.getLength(); i++) {
				Node emptyTextNode = emptyTextNodes.item(i);
				emptyTextNode.getParentNode().removeChild(emptyTextNode);
			}

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("FontSize");

			HashMap<Double, MutableInteger> fontSizeCounter = new LinkedHashMap<Double, MutableInteger>();

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node node = nList.item(temp);
				Integer incVal = 1;
				try {
					Node textNode = node.getParentNode().getParentNode().getNextSibling();

					if (textNode.getNodeName().equalsIgnoreCase("Text")) {
						incVal = textNode.getTextContent().length();
					}
				} catch (Exception e) {
					// Do Nothing
				}

				BigDecimal bd = new BigDecimal(
						node.getTextContent().substring(0, node.getTextContent().lastIndexOf("p")));
				Double key = bd.setScale(2, RoundingMode.HALF_DOWN).doubleValue();

				MutableInteger value = fontSizeCounter.get(key);

				if (value != null) {
					value.set(value.get() + incVal);
				} else {
					fontSizeCounter.put(key, new MutableInteger(incVal));
				}
			}

			if (!fontSizeCounter.isEmpty()) {
				Map<Double, Object> sortedByValueDesc = fontSizeCounter.entrySet().stream()
						.sorted((e1, e2) -> e2.getValue().get().compareTo(e1.getValue().get())).collect(Collectors
								.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

				//System.out.println(Arrays.asList(sortedByValueDesc));

				Double baseFontSize = (Double) sortedByValueDesc.keySet().toArray()[0];

				sortedByValueDesc.entrySet().removeIf(e -> e.getKey() < baseFontSize);

				Map<Double, Object> sortedByValueDesc2 = sortedByValueDesc.entrySet().stream().limit(5).collect(
						Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

				List<Double> sortedByKeyDesc = sortedByValueDesc2.entrySet().stream()
						.sorted((e1, e2) -> (e2.getKey()).compareTo(e1.getKey())).map(e -> e.getKey())
						.collect(Collectors.toCollection(LinkedList::new));

				//System.out.println(Arrays.asList(sortedByKeyDesc));

				JSONObject ConfigJsonObj = new JSONObject();
				JSONObject templateObj = new JSONObject();

				JSONArray headingJsonArray = new JSONArray();

				Double maxFontSize = Double.valueOf(99);

				for (int i = 0; i < sortedByKeyDesc.size(); i++) {
					Double minFontSize = sortedByKeyDesc.get(i);

					JSONObject queryObj = new JSONObject();
					if (minFontSize <= 10)
						queryObj.put("font-name", "Bold");
					queryObj.put("max-font-size", Double.valueOf(maxFontSize));
					queryObj.put("min-font-size", i == sortedByKeyDesc.size() - 1 ? minFontSize + 0.001 : minFontSize);

					JSONObject headingObj = new JSONObject();
					headingObj.put("query", queryObj);
					headingObj.put("style", "h" + (i + 1));

					headingJsonArray.add(headingObj);

					maxFontSize = minFontSize;
				}

				templateObj.put("heading", headingJsonArray);
				ConfigJsonObj.put("template", templateObj);

				try (FileWriter file = new FileWriter(baseNameWithPath + ".json")) {
					file.write(ConfigJsonObj.toJSONString());
					System.out.println("Successfully Copied JSON Object to File...");
					System.out.println("\nJSON Object: " + ConfigJsonObj);
					configPath = baseNameWithPath + ".json";
				}
			} else {
				System.out.println("No Fonts present......");
				configPath = Utils.getAbsolutePath("resources/config_default.json");
			}

		} catch (Exception e) {
			System.out.println("Couldnot generate config JSON...");
			e.printStackTrace();
			Utils.getAbsolutePath("resources/config_default.json");
		}

		return configPath;
	}

	public static void addTags(String openPath, String outputFilePath, String configPath) {

		System.out.println("Inside addTags....");

		try {
			String email = "shahid@codemantra.in"; // authorization email
			String licenseKey = "gaXz94Eomk512fev"; // license key

			if (configPath == null)
				configPath = Utils.getAbsolutePath("resources/config_default.json"); // configuration file

			new File(outputFilePath.substring(0, outputFilePath.lastIndexOf("/"))).mkdirs();

			AddTags.run(email, licenseKey, openPath, outputFilePath, configPath);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/*protected void finalize() throws Throwable {
		System.out.println("Inside finally...........");
		try {
			watcher.close();
		} catch (IOException e) {
			System.out.println("Error closing watcher service" + e);
		}
		executor.shutdown();
	}*/

}
