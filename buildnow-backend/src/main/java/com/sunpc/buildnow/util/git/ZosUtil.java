/**
 *  Class: util.ZosUtil
 * 
 *	Author: Peng Cheng Sun
 *   
 *  Modification History
 *  01. 12/10/2018: Initial version.
 *  02. 12/18/2018: Added mappingMemberName().
 */
package com.sunpc.buildnow.util.git;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author PengChengSun
 *
 */
public class ZosUtil {

	public static List<Map<String, Object>> readPromoteMember(String promoteMemberName) throws IOException {
		// read the file
		String pmCont = TextProcessor.readFile(promoteMemberName);
		String[] pmContArray = pmCont.split("\n");

		// define a list
		List<Map<String, Object>> listMember = new ArrayList<Map<String, Object>>();

		for (String pmStr : pmContArray) {
			pmStr = pmStr.toUpperCase().trim();
			if (pmStr.length() > 0 && !pmStr.startsWith("*") && !pmStr.startsWith("%")) {
				System.out.println(pmStr);
				String[] pmEntryArray = pmStr.split(" ");
				List<String> pmEntryList = new ArrayList<String>();
				for (String pmEntry : pmEntryArray) {
					if (!pmEntry.trim().equals("")) {
						pmEntryList.add(pmEntry.trim());
					}
				}
				if (pmEntryList.size() > 1) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("name", mappingMemberName(pmEntryList.get(0), pmEntryList.get(1)));
					map.put("lib", pmEntryList.get(1));
					map.put("localName", mappingLocalName(pmEntryList.get(0), pmEntryList.get(1)));
					listMember.add(map);
				}
			}
		}

		return listMember;
	}

	public static String mappingMemberName(String name, String lib) {
		// name mapping - defect found by Allen 12/18/2018
		if (name.startsWith("FMS")
				&& (lib.equals("INSTJCL") || lib.equals("INSTT") || lib.equals("RUN") || lib.equals("RUNT"))) {
			name = "DMD" + name.substring(3);
		}
		return name;
	}

	public static String mappingLocalName(String name, String lib) {
		// IDz mapping - provided by Allen 12/10/2018
		String suffix = "";
		if (lib.equals("PLINCL")) {
			suffix = ".inc";
		} else if (lib.equals("PLI")) {
			suffix = ".pli";
		} else if (lib.equals("DB2UTIL") || lib.equals("DB2UTT") || lib.equals("QMFQUERY") || lib.equals("QMFQT")
				|| lib.equals("SQLPL") || lib.equals("UTILDB2") || lib.equals("UTILDT")) {
			suffix = ".sql";
		} else if (lib.equals("INSTJCL") || lib.equals("INSTT") || lib.equals("RUN") || lib.equals("RUNT")
				|| lib.equals("PROCLIB") || lib.equals("PROCT") || lib.equals("SKLJCL") || lib.equals("SKLRUN")
				|| lib.equals("SKLPROC")) {
			suffix = ".jcl";
		} else if (lib.equals("CLIST") || lib.equals("EXEC")) {
			suffix = ".rex";
		} else if (lib.equals("LOAD")) {
			suffix = ".exe";
		} else if (lib.equals("AZUCFG")) {
			suffix = ".azucfg";
		} else if (lib.equals("AZURES")) {
			suffix = ".azures";
		} else if (lib.equals("AZUGEN")) {
			suffix = ".xml";
		} else if (lib.equals("AZUSCH")) {
			suffix = ".xsd";
		} else if (lib.equals("AZUTDT")) {
			suffix = ".xml";
		}

		return mappingMemberName(name, lib) + suffix;
	}

	public static void formatPLI(String pliName) throws IOException {
		// read file
		String pli = TextProcessor.readFile(pliName);

		// replace ^ to ¬
		pli = TextProcessor.replaceStr(pli, "^", TextProcessor.hexStringToString("C2AC"));
		
		//pli = TextProcessor.replaceStr(pli, "?", TextProcessor.hexStringToString("C2AC"));
		pli = TextProcessor.replaceStr(pli, "'?'", "$$JIM$$");
		pli = TextProcessor.replaceStr(pli, TextProcessor.hexStringToString("3F"), TextProcessor.hexStringToString("C2AC"));
		pli = TextProcessor.replaceStr(pli, "$$JIM$$", "'?'");

		// write file
		TextProcessor.writeFile(pli, pliName);
	}

}
