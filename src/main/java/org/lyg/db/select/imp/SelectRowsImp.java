package org.lyg.db.select.imp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.lyg.cache.CacheManager;
@SuppressWarnings({ "unused", "unchecked" })
public class SelectRowsImp {
	public static List<Map<String, Object>> SelectRowsByAttribute(String currentDB, String tableName, String culmnName, Object value) throws IOException{
		if(value==null) {
			value="";
		}
		String objectType = "";
		List<Map<String, Object>> output = new ArrayList<>();
		//锁定数据库
		String DBPath = CacheManager.getCacheInfo("DBPath").getValue().toString() + "/" + currentDB;
		//锁定表
		File fileDBPath = new File(DBPath);
		if (fileDBPath.isDirectory()) {
			String DBTablePath = DBPath + "/" + tableName;
			File fileDBTable = new File(DBTablePath);
			if (fileDBTable.isDirectory()) {
				String DBTableCulumnPath = DBTablePath + "/spec/" + culmnName;
				File fileDBTableCulumn = new File(DBTableCulumnPath);
				if (fileDBTableCulumn.isDirectory()) {
					//读取列数据格式
					String[] fileList = fileDBTableCulumn.list();
					File readDBTableSpecCulumnFile = new File(DBTableCulumnPath + "/" + fileList[0]);
					BufferedReader reader = new BufferedReader(new FileReader(readDBTableSpecCulumnFile));  
					String tempString = null;
					while ((tempString = reader.readLine()) != null) {  
						objectType = tempString;			
					}
					reader.close();
					if(objectType.contains("string")) {
						String DBTableRowsPath = DBTablePath + "/rows";	
						File fileDBTableRowsPath = new File(DBTableRowsPath);
						if (fileDBTableRowsPath.isDirectory()) {
							String[] rowList = fileDBTableRowsPath.list();
							NextRow:
								for(String row: rowList) {
									Map<String, Object> rowMap = new HashMap<>();
									String DBTableRowIndexPath = DBTablePath + "/rows/" + row;	
									File readDBTableRowIndexFile = new File(DBTableRowIndexPath);
									if (readDBTableRowIndexFile.isDirectory()) {	
										String isDelete = DBTableRowIndexPath + "/is_delete_1" ;	
										File isDeleteFile = new File(isDelete);
										if(isDeleteFile.exists()) {
											continue NextRow;
										}
										String DBTableRowIndexCulumnPath = DBTableRowIndexPath + "/" + culmnName;	
										File readDBTableRowIndexCulumnFile = new File(DBTableRowIndexCulumnPath);
										if (readDBTableRowIndexCulumnFile.isDirectory()) {
											reader = new BufferedReader(new FileReader(readDBTableRowIndexCulumnFile + "/" + "value.lyg"));  
											String temp="";
											while ((tempString = reader.readLine()) != null) {
												temp += tempString;
											}
											reader.close();
											if(temp.equalsIgnoreCase(value.toString())) {
												String[] culumnList = readDBTableRowIndexFile.list();
												NextFile:
													for(String culumn: culumnList) {
														if(culumn.contains("is_delete")) {
															continue NextFile;
														}
														String DBTableCulumnIndexPath = DBTableRowIndexPath + "/" + culumn;	
														File readDBTableCulumnIndexPathFile = new File(DBTableCulumnIndexPath);
														if (readDBTableRowIndexCulumnFile.isDirectory()) {
															reader = new BufferedReader(new FileReader(readDBTableCulumnIndexPathFile + "/" + "value.lyg"));  
															temp="";
															while ((tempString = reader.readLine()) != null) {
																temp += tempString;
															}
															reader.close();
															rowMap.put(culumn, temp);
														}else {
															rowMap.put(culumn, null);
														}
													}
												output.add(rowMap);
											}
										}
									}
								} 
						}
					}
				}
			}
		}
		return output;
	}
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		try {
			new SelectRowsImp().SelectRowsByAttribute("backend", "login", "usr_name", "yaoguangluo");
			// deletefile("D:/file");
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
		}
		System.out.println("ok");
	}

	public static Map<String, Object> selectRowsByTablePath(String tablePath, String pageBegin, String pageEnd, String direction) throws IOException {
		Map<String, Object> output = new HashMap<>();
		int totalPages = 0;
		output.put("tablePath", tablePath);
		int rowBeginIndex = Integer.valueOf(pageBegin);
		int rowEndIndex = Integer.valueOf(pageEnd);
		String objectType = "";
		List<Object> rowMapList = new ArrayList<>();
		File fileDBTable = new File(tablePath);
		if (fileDBTable.isDirectory()) {
			String DBTableRowsPath = tablePath + "/rows";	
			File fileDBTableRowsPath = new File(DBTableRowsPath);
			if (fileDBTableRowsPath.isDirectory()) {
				File[] files = fileDBTableRowsPath.listFiles();
				totalPages = files.length;
				int i = 0;
				int index = 0;
				Here:
					while(i<15) {
						String DBTableRowIndexPath = DBTableRowsPath + "/row" + (direction.contains("next")? rowEndIndex++: --rowBeginIndex);	
						File readDBTableRowIndexFile = new File(DBTableRowIndexPath);
						if (!readDBTableRowIndexFile.exists()) {
							break;
						}
						File deleteTest = new File(DBTableRowIndexPath + "/is_delete_1");
						if (deleteTest.exists()) {
							continue Here;
						}
						i++;
						Map<String, Object> rowMap = new HashMap<>();
						String[] readDBTableRowCulumnsIndexFile = readDBTableRowIndexFile.list();
						Map<String, Object> culumnMaps = new HashMap<>();
						NextFile:
							for(String readDBTableRowCulumnIndexFile: readDBTableRowCulumnsIndexFile) {
								if(readDBTableRowCulumnIndexFile.contains("is_delete")) {
									continue NextFile;
								}
								Map<String, Object> culumnMap = new HashMap<>();
								String DBTableRowIndexCulumnPath = DBTableRowIndexPath + "/" + readDBTableRowCulumnIndexFile;	
								File readDBTableRowIndexCulumnFile = new File(DBTableRowIndexCulumnPath);
								if (readDBTableRowIndexCulumnFile.exists()) {
									String temp = "";
									FileInputStream fis = new FileInputStream(new File(DBTableRowIndexCulumnPath + "/value.lyg"));
									BufferedInputStream bis = new BufferedInputStream(fis);
									byte[] buffer = new byte[1024];
									int cnt;
									while((cnt = bis.read(buffer)) != -1) {
										temp += new String(buffer, 0, cnt);
									}
									fis.close();
									bis.close(); 
									culumnMap.put("culumnName", readDBTableRowCulumnIndexFile);
									culumnMap.put("culumnValue", temp);
									culumnMaps.put(readDBTableRowCulumnIndexFile, culumnMap);
								}
							} 
						rowMap.put("rowValue", culumnMaps);
						if(direction.contains("next")) {
							rowMap.put("rowIndex", rowEndIndex-1);
							rowMapList.add(rowMap);
						}else {
							rowMap.put("rowIndex", rowBeginIndex);
							rowMapList.add(0, rowMap);
						}
					}
			}
		}
		if(direction.contains("next")) {
			output.put("pageBegin", Integer.valueOf(pageEnd));
			output.put("pageEnd", rowEndIndex);
			output.put("totalPages", totalPages);
		}else {
			output.put("pageBegin", rowBeginIndex);
			output.put("pageEnd", Integer.valueOf(pageBegin));
			output.put("totalPages", totalPages);
		}
		output.put("obj", rowMapList);
		List<Object> spec= new ArrayList<>();
		Map<String, Object> row = (Map<String, Object>) rowMapList.get(0);
		Map<String, Object> culumns = (Map<String, Object>) row.get("rowValue");

		Iterator<String> it=culumns.keySet().iterator();
		while(it.hasNext()) {
			spec.add(((Map<String, Object>)culumns.get(it.next())).get("culumnName").toString());
		}
		output.put("spec", spec);
		return output;
	}

	public static Object SelectRowsByAttributes(Map<String, Object> object) throws IOException {
		String objectType = "";
		List<Map<String, Object>> output = new ArrayList<>();
		//锁定数据库
		String DBPath = CacheManager.getCacheInfo("DBPath").getValue().toString() + "/" + object.get("baseName").toString();
		//锁定表
		File fileDBPath = new File(DBPath);
		if (fileDBPath.isDirectory()) {
			String DBTablePath = DBPath + "/" + object.get("tableName").toString();
			File fileDBTable = new File(DBTablePath);
			if (fileDBTable.isDirectory()) {
				String DBTableCulumnPath = DBTablePath + "/spec/" + object.get("tableName").toString();
				File fileDBTableCulumn = new File(DBTableCulumnPath);
				if (fileDBTableCulumn.isDirectory()) {
					//读取列数据格式
					String[] fileList = fileDBTableCulumn.list();
					File readDBTableSpecCulumnFile = new File(DBTableCulumnPath + "/" + fileList[0]);
					BufferedReader reader = new BufferedReader(new FileReader(readDBTableSpecCulumnFile));  
					String tempString = null;
					while ((tempString = reader.readLine()) != null) {  
						objectType = tempString;			
					}
					reader.close();
					if(objectType.contains("string")) {
						//condition
						List<String[]> conditionValues = (List<String[]>) object.get("condition");
						Iterator<String[]> iterator = conditionValues.iterator();
						while(iterator.hasNext()) {
							boolean overMap = output.size() == 0? false: true;
							String[] conditionValueArray = iterator.next();
							overMap = conditionValueArray[1].equalsIgnoreCase("or")?false:true;
							for(int i = 2; i < conditionValueArray.length-1; i++) {
								String[] sets = conditionValueArray[i].split("|");
								if(overMap) {
									processMap(sets, reader, tempString, output, DBTablePath);
								}else{
									processTable(sets, reader, tempString, output, DBTablePath);
								}
							}
						}
					}
				}
			}
		}
		return output;
	}

	private static void processMap(String[] sets, BufferedReader reader, String tempString
			, List<Map<String, Object>> output, String dBTablePath) {
		List<Map<String, Object>> outputTemp = new ArrayList<>();
		Iterator<Map<String, Object>> iterator = output.iterator();
		int rowid = 0;
		output.clear();
		while(iterator.hasNext()) {
			Map<String, Object> row = iterator.next();
			Map<String, Object> rowMap = new HashMap<>();
			BigDecimal bigDecimal = new BigDecimal(row.get(sets[0]).toString());
			if(sets[1].equalsIgnoreCase("<")||sets[1].equalsIgnoreCase("-lt")) {
				if(new BigDecimal(row.get(sets[0]).toString()).doubleValue() < new BigDecimal(sets[2]).doubleValue()) {
					output.add(row);
				}	
			}
			if(sets[1].equalsIgnoreCase("<=")||sets[1].equalsIgnoreCase("=<")
					||sets[1].equalsIgnoreCase("-lte")) {
				if(new BigDecimal(row.get(sets[0]).toString()).doubleValue() <=  new BigDecimal(sets[2]).doubleValue()) {
					output.add(row);
				}	
			}
			if(sets[1].equalsIgnoreCase("==")||sets[1].equalsIgnoreCase("=")
					||sets[1].equalsIgnoreCase("===")||sets[1].equalsIgnoreCase("-eq")) {
				if(new BigDecimal(row.get(sets[0]).toString()).doubleValue() ==  new BigDecimal(sets[2]).doubleValue()) {
					output.add(row);
				}	
			}
			if(sets[1].equalsIgnoreCase(">=")||sets[1].equalsIgnoreCase("=>") 
					||sets[1].equalsIgnoreCase("-gte")) {
				if(new BigDecimal(row.get(sets[0]).toString()).doubleValue() >= new BigDecimal(sets[2]).doubleValue()) {
					output.add(row);
				}	
			}
			if(sets[1].equalsIgnoreCase(">")||sets[1].equalsIgnoreCase("-gt")) {
				if(new BigDecimal(row.get(sets[0]).toString()).doubleValue() > new BigDecimal(sets[2]).doubleValue()) {
					output.add(row);
				}	
			}
			if(sets[1].equalsIgnoreCase("!=")||sets[1].equalsIgnoreCase("=!")
					||sets[1].equalsIgnoreCase("-!eq")||sets[1].equalsIgnoreCase("-eq!")) {
				if(new BigDecimal(row.get(sets[0]).toString()).doubleValue() != new BigDecimal(sets[2]).doubleValue()) {
					output.add(row);
				}	
			}
		}
	}
	private static void processTable(String[] sets, BufferedReader reader, String tempString
			, List<Map<String, Object>> output, String DBTablePath) throws IOException {
		String DBTableRowsPath = DBTablePath + "/rows";	
		File fileDBTableRowsPath = new File(DBTableRowsPath);
		if (fileDBTableRowsPath.isDirectory()) {
			String[] rowList = fileDBTableRowsPath.list();
			NextRow:
				for(String row: rowList) {
					Map<String, Object> rowMap = new HashMap<>();
					String DBTableRowIndexPath = DBTablePath + "/rows/" + row;	
					File readDBTableRowIndexFile = new File(DBTableRowIndexPath);
					if (readDBTableRowIndexFile.isDirectory()) {	
						String isDelete = DBTableRowIndexPath + "/is_delete_1" ;	
						File isDeleteFile = new File(isDelete);
						if(isDeleteFile.exists()) {
							continue NextRow;
						}
						String DBTableRowIndexCulumnPath = DBTableRowIndexPath + "/" + sets[0];	
						File readDBTableRowIndexCulumnFile = new File(DBTableRowIndexCulumnPath);
						if(readDBTableRowIndexCulumnFile.isDirectory()) {
							reader = new BufferedReader(new FileReader(readDBTableRowIndexCulumnFile + "/" + "value.lyg"));  
							String temp = "";
							while ((tempString = reader.readLine()) != null) {
								temp += tempString;
							}
							reader.close();
							if(sets[1].equalsIgnoreCase("<")||sets[1].equalsIgnoreCase("-lt")) {
								if(new BigDecimal(temp.toString()).doubleValue() < new BigDecimal(sets[2].toString()).doubleValue()) {
									processkernel(row, readDBTableRowIndexCulumnFile, readDBTableRowIndexCulumnFile, reader
											, row, output, row, rowMap);
								}	
							}
							if(sets[1].equalsIgnoreCase("<=")||sets[1].equalsIgnoreCase("=<")
									||sets[1].equalsIgnoreCase("-lte")) {
								if(new BigDecimal(temp.toString()).doubleValue() <= new BigDecimal(sets[2].toString()).doubleValue()) {
									processkernel(row, readDBTableRowIndexCulumnFile, readDBTableRowIndexCulumnFile, reader
											, row, output, row, rowMap);
								}	
							}
							if(sets[1].equalsIgnoreCase("==")||sets[1].equalsIgnoreCase("=")
									||sets[1].equalsIgnoreCase("===")||sets[1].equalsIgnoreCase("-eq")) {
								if(new BigDecimal(temp.toString()).doubleValue() == new BigDecimal(sets[2].toString()).doubleValue()) {
									processkernel(row, readDBTableRowIndexCulumnFile, readDBTableRowIndexCulumnFile, reader
											, row, output, row, rowMap);
								}	
							}
							if(sets[1].equalsIgnoreCase(">=")||sets[1].equalsIgnoreCase("=>") 
									||sets[1].equalsIgnoreCase("-gte")) {
								if(new BigDecimal(temp.toString()).doubleValue() >= new BigDecimal(sets[2].toString()).doubleValue()) {
									processkernel(row, readDBTableRowIndexCulumnFile, readDBTableRowIndexCulumnFile, reader
											, row, output, row, rowMap);
								}	
							}
							if(sets[1].equalsIgnoreCase(">")||sets[1].equalsIgnoreCase("-gt")) {
								if(new BigDecimal(temp.toString()).doubleValue() > new BigDecimal(sets[2].toString()).doubleValue()) {
									processkernel(row, readDBTableRowIndexCulumnFile, readDBTableRowIndexCulumnFile, reader
											, row, output, row, rowMap);
								}	
							}
							if(sets[1].equalsIgnoreCase("!=")||sets[1].equalsIgnoreCase("=!")
									||sets[1].equalsIgnoreCase("-!eq")||sets[1].equalsIgnoreCase("-eq!")) {
								if(new BigDecimal(temp.toString()).doubleValue() != new BigDecimal(sets[2].toString()).doubleValue()) {
									processkernel(row, readDBTableRowIndexCulumnFile, readDBTableRowIndexCulumnFile, reader
											, row, output, row, rowMap);
								}	
							}
						}
					} 
				}
		}
	}
	
	private static void processkernel(String temp, File readDBTableRowIndexCulumnFile, File readDBTableRowIndexFile
			, BufferedReader reader, String DBTableRowIndexPath, List<Map<String, Object>> output, String tempString
			, Map<String, Object> rowMap) throws IOException {
		String[] culumnList = readDBTableRowIndexFile.list();
		NextFile:
			for(String culumn: culumnList) {
				if(culumn.contains("is_delete")) {
					continue NextFile;
				}
				String DBTableCulumnIndexPath = DBTableRowIndexPath + "/" + culumn;	
				File readDBTableCulumnIndexPathFile = new File(DBTableCulumnIndexPath);
				if (readDBTableRowIndexCulumnFile.isDirectory()) {
					reader = new BufferedReader(new FileReader(readDBTableCulumnIndexPathFile + "/" + "value.lyg"));  
					temp="";
					while ((tempString = reader.readLine()) != null) {
						temp += tempString;
					}
					reader.close();
					rowMap.put(culumn, temp);
				}else {
					rowMap.put(culumn, null);
				}
			}
		output.add(rowMap);
	}

	public static Object SelectRowsByJoinAttributes(Map<String, Object> object) {
		return null;
	}
}