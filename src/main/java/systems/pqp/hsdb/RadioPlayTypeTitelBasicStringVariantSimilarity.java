package systems.pqp.hsdb;

import de.ard.sad.normdb.similarity.model.generic.GenricObjectType.SimAlgorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RadioPlayTypeTitelBasicStringVariantSimilarity implements SimAlgorithm {

	SimAlgorithm basicSimAlgorithm;
	OutputSetting output;
	boolean extractLeftSidedPart;
	boolean compareVariantsAgainstEachOther;

	final Pattern bracketPattern = Pattern.compile("\".*\""+
			"|'.*'" +
			"|\\(.*\\)"+
			"|\\{.*\\}"+
			"|\\[.*\\]");

	public enum OutputSetting {MIN,MAX};

	public RadioPlayTypeTitelBasicStringVariantSimilarity(SimAlgorithm basicSimAlgorithm, OutputSetting output){
		this(basicSimAlgorithm,output,false);
	}

	public RadioPlayTypeTitelBasicStringVariantSimilarity(SimAlgorithm basicSimAlgorithm, OutputSetting output, boolean extractLeftSidedPart){
		this(basicSimAlgorithm,output,extractLeftSidedPart,false);
	}

	public RadioPlayTypeTitelBasicStringVariantSimilarity(SimAlgorithm basicSimAlgorithm, OutputSetting output, boolean extractLeftSidedPart, boolean compareVariantsAgainstEachOther){
		this.basicSimAlgorithm = basicSimAlgorithm;
		this.output = output;
		this.extractLeftSidedPart = extractLeftSidedPart;
		this.compareVariantsAgainstEachOther = compareVariantsAgainstEachOther;
	}

	public float calcSimilarity(String pattern, String target) {
		//Originale Strings vergleichen
		boolean allowContainCheck = false;
		float nativeSim = calcSimilarityIntern(pattern,target,allowContainCheck);
		List<String> alreadyCalculated = new ArrayList<>();
		String check = pattern+ " <-> "+target +" <-> "+allowContainCheck;
		alreadyCalculated.add(check);

		float minSim = nativeSim;
		float maxSim = nativeSim;

		Set<String> patternVariantsDefault = generateStringVariants(pattern);
		Set<String> targetVariantsDefault = generateStringVariants(target);

		Set<String> patternVariantsExtended = new HashSet<>(patternVariantsDefault);
		Set<String> targetVariantsExtended = new HashSet<>(targetVariantsDefault);

		//patternVariantsExtended.addAll(patternVariantsDefault);
		patternVariantsExtended.addAll(generateStringVariantsExtended(pattern));
		//targetVariantsExtended.addAll(targetVariantsDefault);
		targetVariantsExtended.addAll(generateStringVariantsExtended(target));

		Set<String> patternVariants = patternVariantsDefault;
		Set<String> targetVariants = targetVariantsDefault;

		allowContainCheck = false;

		//Verschiedene Varianten mit einander vergleichen
		if(compareVariantsAgainstEachOther==true){
			if(extractLeftSidedPart==false) {
				if(maxSim<1.0f) {
					loop:
					for (String patternVariant : patternVariants) {
						for (String targetVariant : targetVariants) {
							check = patternVariant + " <-> " + targetVariant +" <-> "+allowContainCheck;
							if (alreadyCalculated.contains(check) == false) {
								alreadyCalculated.add(check);
								float curSim = calcSimilarityIntern(patternVariant, targetVariant,allowContainCheck);
								if (curSim > maxSim) {
									maxSim = curSim;
									if (maxSim == 1.0f)
										break loop;
								}
								if (curSim < minSim) {
									minSim = curSim;
								}
							}
						}
					}
				}
			}else{
				if(maxSim<1.0f) {
					loop:
					for (String patternVariant : patternVariantsExtended) {
						for (String targetVariant : targetVariantsDefault) {
							check = patternVariant + " <-> " + targetVariant +" <-> "+allowContainCheck;
							if (alreadyCalculated.contains(check) == false) {
								alreadyCalculated.add(check);
								float curSim = calcSimilarityIntern(patternVariant, targetVariant,allowContainCheck);
								if (curSim > maxSim) {
									maxSim = curSim;
									if (maxSim == 1.0f)
										break loop;
								}
								if (curSim < minSim) {
									minSim = curSim;
								}
							}
						}
					}
				}

				if(maxSim<1.0f) {
					loop:
					for (String patternVariant : patternVariantsDefault) {
						for (String targetVariant : targetVariantsExtended) {
							check = patternVariant + " <-> " + targetVariant +" <-> "+allowContainCheck;
							if (alreadyCalculated.contains(check) == false) {
								alreadyCalculated.add(check);
								float curSim = calcSimilarityIntern(patternVariant, targetVariant,allowContainCheck);
								if (curSim > maxSim) {
									maxSim = curSim;
									if (maxSim == 1.0f)
										break loop;
								}
								if (curSim < minSim) {
									minSim = curSim;
								}
							}
						}
					}
				}
			}
		}

		//Verschiedene Patternvarianten vergleichen
		allowContainCheck = false;
		if(extractLeftSidedPart) {
			patternVariants = patternVariantsExtended;
			targetVariants = targetVariantsExtended;
		}else{
			patternVariants = patternVariantsDefault;
			targetVariants = targetVariantsDefault;
		}

		if(maxSim<1.0f) {
			loop:
			for (String patternVariant : patternVariants) {
				check = patternVariant + " <-> " + target +" <-> "+allowContainCheck;
				if (alreadyCalculated.contains(check) == false) {
					alreadyCalculated.add(check);
					float curSim = calcSimilarityIntern(patternVariant, target,allowContainCheck);
					if (curSim > maxSim) {
						maxSim = curSim;
						if (maxSim == 1.0f)
							break loop;
					}
					if (curSim < minSim) {
						minSim = curSim;
					}
				}
			}
		}

		if(maxSim<1.0f) {
			loop:
			for (String targetVariant : targetVariants) {
				check = pattern + " <-> " + targetVariant +" <-> "+allowContainCheck;
				if (alreadyCalculated.contains(check) == false) {
					alreadyCalculated.add(check);
					float curSim = calcSimilarityIntern(pattern, targetVariant,allowContainCheck);
					if (curSim > maxSim) {
						maxSim = curSim;
						if (maxSim == 1.0f)
							break loop;
					}
					if (curSim < minSim) {
						minSim = curSim;
					}
				}
			}
		}

		//Ergebnis zurückgeben
		if(OutputSetting.MIN.equals(output)) {
			if(minSim >= 1.0f && nativeSim < 1.0f) minSim = 0.99f;	//Geringfügig reduzieren, da eigentlich keine Identität

			return minSim;
		}else{
			if(maxSim >= 1.0f && nativeSim < 1.0f) maxSim = 0.99f;	//Geringfügig reduzieren, da eigentlich keine Identität

			return maxSim;
		}
	}

	protected float calcSimilarityIntern(String pattern, String target, boolean allowContainCheck) {
		return basicSimAlgorithm.calcSimilarity(pattern, target);
	}

	//Varianten eines Strings erzeugen
	public Set<String> generateStringVariantsExtended(String text) {
		text = text.replaceAll("–","-");
		Set<String> results = new HashSet<>();

		int spaceIdx = text.indexOf(" ");

		if(extractLeftSidedPart == true) {
			String tmp = text.replaceAll("[-|]+", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
			tmp = tmp.replaceAll("\\s+", " ").trim();	//unnötige Leerzeichen entfernen
			//Bereich vor Bindestrich extrahieren
			int idx = findSeperator(text,"- ");
			if (idx >= 0) {
				tmp = text.substring(0, idx).trim(); //unnötige Leerzeichen entfernen
				if (text.equals(tmp) == false  && tmp.length()>0) {
					tmp = tmp.replaceAll("\\s+", " ").trim();
					results.add(tmp);
				}
			}

			//Bereich vor letztem Komma entfernen, sofern danach kein weiteres Sonderzeichen vorkommt
			idx = findSeperator(text,",");

			if (idx >= 0 && (spaceIdx==-1 || spaceIdx < idx)) {
				tmp = text.substring(0, idx).trim(); //unnötige Leerzeichen entfernen
				if (text.equals(tmp) == false  && tmp.length()>0) {
					tmp = tmp.replaceAll("\\s+", " ").trim();
					results.add(tmp);
				}
			}

			//Bereich vor Punkt extrahieren
			idx = findSeperator(text,". ");
			if (idx >= 0 && (spaceIdx==-1 || spaceIdx < idx)) {
				tmp = text.substring(0, idx).trim(); //unnötige Leerzeichen entfernen
				if (text.equals(tmp) == false  && tmp.length()>0) {
					tmp = tmp.replaceAll("\\s+", " ").trim();
					results.add(tmp);
				}
			}

			//Bereich vor Doppelpunkt extrahieren
			/*idx = findSeperator(text,": ");
			if (idx >= 0) {
				tmp = text.substring(0, idx).trim(); //unnötige Leerzeichen entfernen
				if (text.equals(tmp) == false  && tmp.length()>0) {
					tmp = tmp.replaceAll("\\s+", " ").trim();
					results.add(tmp);
				}
			}*/
		}

		/*
		//Runde Klammern komplett entfernen
		String tmp = text.replaceAll("\\(.*\\)", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
		tmp = tmp.replaceAll("\\s+", " ").trim();	//unnötige Leerzeichen entfernen
		if(text.equals(tmp)==false && tmp.length()>0)
			results.add(tmp);

		//Geschweifte Klammern komplett entfernen
		tmp = DataExtractor.removeBracketsWithoutSeasonAndEpisode(text,geschweifteKlammernPattern);
		tmp = text.replaceAll("\\{.*\\}", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
		tmp = tmp.replaceAll("\\s+", " ").trim();	//unnötige Leerzeichen entfernen
		if(text.equals(tmp)==false && tmp.length()>0)
			results.add(tmp);

		//Eckige Klammern komplett entfernen
		tmp = DataExtractor.removeBracketsWithoutSeasonAndEpisode(text,eckigeKlammernPattern);
		tmp = text.replaceAll("\\[.*\\]", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
		tmp = tmp.replaceAll("\\s+", " ").trim();	//unnötige Leerzeichen entfernen
		if(text.equals(tmp)==false && tmp.length()>0)
			results.add(tmp);

		 */

		//Alle Klammerarten komplett entfernen
		String tmp = DataExtractor.removeBracketsWithoutSeasonAndEpisode(text);
		/*tmp = text.replaceAll("\\(.*\\)", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
		tmp = tmp.replaceAll("\\{.*\\}", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
		tmp = tmp.replaceAll("\\[.*\\]", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
		tmp = tmp.replaceAll("\\s+", " ").trim();	//unnötige Leerzeichen entfernen*/
		if(text.equals(tmp)==false && tmp.length()>0)
			results.add(tmp);

		return results;
	}



	public int findSeperator(String text, String seperator){
		int idx = text.indexOf(seperator);
		while(idx!=-1) {
			if(isInBrackets(text, idx)==false) {
				return idx;
			}
			idx = text.indexOf(seperator,idx+1);
		}
		return -1;
	}

	public boolean isInBrackets(String text, int idx) {
		Matcher m = bracketPattern.matcher(text);
		while (m.find()) {
			if(m.start()<idx && m.end()>idx)
				return true;
		}
		return false;
	}

	//Varianten eines Strings erzeugen
	public Set<String> generateStringVariants(String text) {
		Set<String> results = new HashSet<>();

		text = text.replaceAll("–","-");

		//Punkte entfernen
		String tmp = text.replaceAll("\\.+", " ");
		tmp = tmp.replaceAll("\\s+", " ").trim();	//unnötige Leerzeichen entfernen
		if(text.equals(tmp)==false && tmp.length()>0)
			results.add(tmp);

		//Bindestriche entfernen
		tmp = text.replaceAll("[-|]+", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
		tmp = tmp.replaceAll("\\s+", " ").trim();	//unnötige Leerzeichen entfernen
		if(text.equals(tmp)==false && tmp.length()>0)
			results.add(tmp);

		//Klammerzeichen entfernen
		tmp = text.replaceAll("[(){}\\[\\]]", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
		tmp = tmp.replaceAll("\\s+", " ").trim();	//unnötige Leerzeichen entfernen
		if(text.equals(tmp)==false && tmp.length()>0)
			results.add(tmp);

		//Sondernzeichen entfernen
		results.add(text.replaceAll("[^a-zA-Z0-9äöüßÄÖÜ]", " ").replaceAll("\\s+", " ").trim());


		if(extractLeftSidedPart == true) {
			//Bereich vor Bindestrich extrahieren
			/*int idx = text.indexOf(" -") - 1;
			if (idx >= 0) {
				tmp = text.substring(0, idx+1).trim(); //unnötige Leerzeichen entfernen
				if (text.equals(tmp) == false  && tmp.length()>0) {
					tmp = tmp.replaceAll("\\s+", " ").trim();
					results.add(tmp);
				}
			}*/

			//Bereich vor Pipe extrahieren
			int idx = text.indexOf("|") - 1;
			if (idx >= 0) {
				tmp = text.substring(0, idx).trim(); //unnötige Leerzeichen entfernen
				if (text.equals(tmp) == false  && tmp.length()>0) {
					tmp = tmp.replaceAll("\\s+", " ").trim();
					results.add(tmp);
				}
			}

			//Bereich vor letztem Komma entfernen, sofern danach kein weiteres Sonderzeichen vorkommt
			/*idx = text.lastIndexOf(",");
			if (idx >= 0 && idx > text.lastIndexOf(")",idx) && idx > text.lastIndexOf("}",idx) && idx > text.lastIndexOf("\\]",idx) && (text.indexOf("\"")==-1||(idx > text.lastIndexOf("\"",idx))&& idx < text.indexOf("\""))) {
				tmp = text.substring(0, idx).trim(); //unnötige Leerzeichen entfernen
				if (text.equals(tmp) == false  && tmp.length()>0) {
					tmp = tmp.replaceAll("\\s+", " ").trim();
					results.add(tmp);
				}
			}*/
		}

		/*
		//Runde Klammern komplett entfernen
		tmp = text.replaceAll("\\(.*\\)", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
		tmp = tmp.replaceAll("\\s+", " ").trim();	//unnötige Leerzeichen entfernen
		if(text.equals(tmp)==false && tmp.length()>0)
			results.add(tmp);

		//Geschweifte Klammern komplett entfernen
		tmp = text.replaceAll("\\{.*\\}", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
		tmp = tmp.replaceAll("\\s+", " ").trim();	//unnötige Leerzeichen entfernen
		if(text.equals(tmp)==false && tmp.length()>0)
			results.add(tmp);

		//Eckige Klammern komplett entfernen
		tmp = text.replaceAll("\\[.*\\]", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
		tmp = tmp.replaceAll("\\s+", " ").trim();	//unnötige Leerzeichen entfernen
		if(text.equals(tmp)==false && tmp.length()>0)
			results.add(tmp);

		//Alle Klammerarten komplett entfernen
		tmp = text.replaceAll("\\(.*\\)", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
		tmp = tmp.replaceAll("\\{.*\\}", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
		tmp = tmp.replaceAll("\\[.*\\]", " ");	//sorgt dafür, dass Doppelnamen auch erkannt werden können wenn nur einer der beiden Namen mit - Verbunden
		tmp = tmp.replaceAll("\\s+", " ").trim();	//unnötige Leerzeichen entfernen
		if(text.equals(tmp)==false && tmp.length()>0)
			results.add(tmp);
		 */

		return results;
	}
}