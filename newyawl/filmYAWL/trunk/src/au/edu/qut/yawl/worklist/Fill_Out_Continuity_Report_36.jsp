<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.math.BigInteger" %>
<%@ page import="com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl" %>
<%@ page import="javax.xml.bind.JAXBElement" %>
<%@ page import="javax.xml.bind.JAXBContext" %>
<%@ page import="javax.xml.bind.Marshaller" %>
<%@ page import="javax.xml.bind.Unmarshaller" %>
<%@ page import="org.yawlfoundation.sb.continuityinfo.*"%>
<%@ page buffer="1024kb" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Continuity Report</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="javascript">
var count = 1;
function addRow()
{
var tbody = document.getElementById("table1").getElementsByTagName("tbody")[0];
var row = document.createElement("TR");
var cell1 = document.createElement("TD");
var cell2 = document.createElement("TD");
var cell3 = document.createElement("TD");
var cell4 = document.createElement("TD");
var cell5 = document.createElement("TD");
var cell6 = document.createElement("TD");
var cell7 = document.createElement("TD");
var cell8 = document.createElement("TD");
var inp1 =  document.createElement("INPUT");
var inp2 =  document.createElement("INPUT");
var inp3 =  document.createElement("INPUT");
var inp4 =  document.createElement("INPUT");
var inp5 =  document.createElement("INPUT");
var inp6 =  document.createElement("INPUT");
var inp7 =  document.createElement("INPUT");
var inp8 =  document.createElement("TEXTAREA");

var previous_cam = document.getElementById("cam_" + count);
var previous_sound = document.getElementById("sound_" + count);
var previous_scene = document.getElementById("scene_" + count);
var previous_slate = document.getElementById("slate_" + count);

count ++;

var current_cam = "cam_" + count;
var current_sound = "sound_" + count;
var current_scene = "scene_" + count;
var current_slate = "slate_" + count;
var current_take = "take_" + count;
var current_print = "print_" + count;
var current_duration = "duration_" + count;
var current_comments = "comments_" + count;

row.setAttribute("valign", "top");
row.setAttribute("align", "center");

inp1.setAttribute("size",  "10");
inp1.setAttribute("value", previous_cam.value);
inp1.setAttribute("name", current_cam);
inp1.setAttribute("id", current_cam);

inp2.setAttribute("size", "10");
inp2.setAttribute("value", previous_sound.value);
inp2.setAttribute("name", current_sound);
inp2.setAttribute("id", current_sound);

inp3.setAttribute("size", "10");
inp3.setAttribute("value", previous_scene.value);
inp3.setAttribute("name", current_scene);
inp3.setAttribute("id", current_scene);

inp4.setAttribute("size", "10");
inp4.setAttribute("value", previous_slate.value);
inp4.setAttribute("name", current_slate);
inp4.setAttribute("id", current_slate);

inp5.setAttribute("size", "10");
inp5.setAttribute("name", current_take);
inp5.setAttribute("id", current_take);

inp6.setAttribute("type", "checkbox");
inp6.setAttribute("value", "True");
inp6.setAttribute("name", current_print);
inp6.setAttribute("id", current_print);

inp7.setAttribute("size", "10");
inp7.setAttribute("name", current_duration);
inp7.setAttribute("id", current_duration);

inp8.setAttribute("cols", "50");
inp8.setAttribute("name", current_comments);
inp8.setAttribute("id", current_comments);

cell1.appendChild(inp1);
cell2.appendChild(inp2);
cell3.appendChild(inp3);
cell4.appendChild(inp4);
cell5.appendChild(inp5);
cell6.appendChild(inp6);
cell7.appendChild(inp7);
cell8.appendChild(inp8);

row.appendChild(cell1);
row.appendChild(cell2);
row.appendChild(cell3);
row.appendChild(cell4);
row.appendChild(cell5);
row.appendChild(cell6);
row.appendChild(cell7);
row.appendChild(cell8);
tbody.appendChild(row);
//alert(row.innerHTML);

}

function getCount (form) {
document.getElementById("count").value = count;
return true;
}

function getParam(name)
{
  var start=location.search.indexOf("?"+name+"=");
  if (start<0) start=location.search.indexOf("&"+name+"=");
  if (start<0) return '';
  start += name.length+2;
  var end=location.search.indexOf("&",start)-1;
  if (end<0) end=location.search.length;
  var result='';
  for(var i=start;i<=end;i++) {
    var c=location.search.charAt(i);
    result=result+(c=='+'?' ':c);
  }
  //window.alert('Result = '+result);
  return unescape(result);
}

function getParameters(){
	document.form1.workItemID.value = getParam('workItemID');
	document.form1.userID.value = getParam('userID');
	document.form1.sessionHandle.value = getParam('sessionHandle');
	document.form1.submit.value = "htmlForm";
}
</script>
</head>

<body onLoad="getParameters()">
<h1>Continuity Report</h1>
<form name="form1" method="post" onSubmit="return getCount(this)">
  <table width="800" border="0">
<% 
	String xml = new String();

	if(request.getParameter("outputData") != null){
				xml = request.getParameter("outputData");
				xml = xml.replaceAll("<Fill_Out_Continuity_Report", "<ns2:Fill_Out_Continuity_Report xmlns:ns2='http://www.yawlfoundation.org/sb/continuityInfo'");
				xml = xml.replaceAll("</Fill_Out_Continuity_Report","</ns2:Fill_Out_Continuity_Report");
				
				ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
				JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.continuityinfo");
				Unmarshaller u = jc.createUnmarshaller();
				JAXBElement focrElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
				FillOutContinuityReportType focr = (FillOutContinuityReportType)focrElement.getValue();
				
				GeneralInfoType gi = focr.getGeneralInfo();
				
				out.println("<tr><td><table width='800'><tr>");
                out.println("<td><strong>PRODUCTION</strong></td><td><input name='production' type='text' id='production' value='"+gi.getProduction()+"' readonly></td><td>&nbsp;</td>");
                out.println("<td><strong>DATE</strong></td><td><input name='date' type='text' id='date' value='"+gi.getDate().getDay()+"-"+gi.getDate().getMonth()+"-"+gi.getDate().getYear()+"' readonly></td><td>&nbsp;</td>");
                out.println("<td><strong>DAY</strong></td><td><input name='weekday' type='text' id='weekday' value='"+gi.getWeekday()+"' readonly></td>");
				out.println("</tr></table></td></tr>");
					
				out.println("<tr><td>&nbsp;</td></tr>");
				out.println("<tr><td><table width='800'><tr>");
				out.println("<td><strong>Producer</strong></td><td><input name='producer' type='text' id='producer' value='"+focr.getProducer()+"' readonly></td>");
				out.println("<td><strong>Director</strong></td><td><input name='director' type='text' id='director' value='"+focr.getDirector()+"' readonly></td>");
				out.println("<td><strong>Shoot Day </strong></td><td><input name='shoot_day' type='text' id='shoot_day' value='"+gi.getShootDayNo()+"' readonly></td>");
				out.println("</tr><tr>");
				out.println("<td><strong>Director of Photography</strong></td><td><input name='director_photography' type='text' id='director_photography' value='"+focr.getDirectorOfPhotography()+"' readonly></td>");
				out.println("<td><strong>Editor</strong></td><td><input name='editor' type='text' id='editor' value='"+focr.getEditor()+"' readonly></td>");
				out.println("<td><strong>Continuity</strong></td><td><input name='continuity' type='text' id='continuity' value='"+focr.getContinuity()+"' readonly></td>");
				out.println("</tr></table></td></tr>");
					
				out.println("<tr><td>&nbsp;</td></tr>");
	}
%>
		
	<tr><td>&nbsp;</td></tr>
	
		<tr>
			<td>
				<table width="800" id="table1">
					<tbody>
						<tr valign="top">
							<th><strong>CAM</strong></th><th><strong>SOUND</strong></th>
							<th><strong>SCENE</strong></th><th><strong>SLATE</strong></th>
							<th><strong>TAKE</strong></th><th><strong>PRINT</strong></th>
							<th><strong>DURATION</strong></th><th><strong>SHOT DESCRIPTION - COMMENTS <br>
							(lens, focus, stop, filter)</strong></th>
						</tr>
						<tr valign="top">
							<td align="center"><input name="cam_1" type="text" id="cam_1" size="10"></td>
							<td align="center"><input name="sound_1" type="text" id="sound_1" size="10"></td>
							<td align="center"><input name="scene_1" type="text" id="scene_1" size="10"></td>
							<td align="center"><input name="slate_1" type="text" id="slate_1" size="10"></td>
							<td align="center"><input name="take_1" type="text" id="take_1" size="10"></td>
							<td align="center"><input name="print_1" type="checkbox" id="print_1" value="True"></td>
							<td align="center"><input name="duration_1" type="text" id="duration_1" size="10"></td>
							<td><textarea name="comments_1" cols="50" id="comments_1"></textarea></td>
						</tr>
					</tbody>
				</table>
			</td>
		</tr>
	
		<tr><td>
			<input type="button" value="Insert Row" onClick="addRow();"/>
			<input type="hidden" name="count" id="count" value="1"/>
			<input type="hidden" name="workItemID" id="workItemID"/>
			<input type="hidden" name="userID" id="userID"/>
			<input type="hidden" name="sessionHandle" id="sessionHandle"/>
			<input type="hidden" name="submit" id="submit"/>
		</td></tr>
  	</table>
  	<p>
  	<input type="submit" name="Save" value="Save"/>
 	<input type="submit" name="Submission" value="Submission"/></p>
</form>

<form action="<%= request.getContextPath() %>/load?cameFrom=Fill_Out_Continuity_Report_36.jsp" method="POST">
	<input type="submit" name="Load" value="Load Report" />
</form>

<%
if (request.getParameter("load") != null){
	System.out.println("Load 1.");
	if (request.getInputStream() != null) {
		System.out.println("Load 2.");
		try {
		
			ServletInputStream in = request.getInputStream();
			StringBuffer result = new StringBuffer();
			
			int i = in.read();
			while (i != -1) {
				result.append((char) i);
				i = in.read();
			}
			String filepath = result.substring(result.indexOf("filename=\"") + 10, result.indexOf("Content-Type") -2 );
			String filename = filepath.substring(filepath.lastIndexOf(File.separator) + 1, filepath.length() -1);
		
			int beginOfXML = result.indexOf("<ns2:Fill_Out_Continuity_Report");//
			int endOfXML = result.indexOf("</ns2:Fill_Out_Continuity_Report>");
			if(beginOfXML != -1 && endOfXML != -1){
				xml = result.substring(beginOfXML,endOfXML + 33);
				
				ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
				JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.continuityinfo");
				Unmarshaller u = jc.createUnmarshaller();
				JAXBElement focrElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
				FillOutContinuityReportType focr = (FillOutContinuityReportType)focrElement.getValue();
				
				GeneralInfoType gi = focr.getGeneralInfo();
				
				out.println("<table width='800' border='0'><tr><td><table width='800'><tr>");
                out.println("<td><strong>PRODUCTION</strong></td><td><input name='production' type='text' id='production' value='"+gi.getProduction()+"' readonly></td><td>&nbsp;</td>");
                out.println("<td><strong>DATE</strong></td><td><input name='date' type='text' id='date' value='"+gi.getDate()+"' readonly></td><td>&nbsp;</td>");
                out.println("<td><strong>DAY</strong></td><td><input name='weekday' type='text' id='weekday' value='"+gi.getWeekday()+"' readonly></td>");
				out.println("</tr></table></td></tr>");
					
				out.println("<tr><td>&nbsp;</td></tr>");
				out.println("<tr><td><table width='800'><tr>");
				out.println("<td><strong>Producer</strong></td><td><input name='producer' type='text' id='producer' value='"+focr.getProducer()+"' readonly></td>");
				out.println("<td><strong>Director</strong></td><td><input name='director' type='text' id='director' value='"+focr.getDirector()+"' readonly></td>");
				out.println("<td><strong>Shoot Day </strong></td><td><input name='shoot_day' type='text' id='shoot_day' value='"+gi.getShootDayNo()+"' readonly></td>");
				out.println("</tr><tr>");
				out.println("<td><strong>Director of Photography</strong></td><td><input name='director_photography' type='text' id='director_photography' value='"+focr.getDirectorOfPhotography()+"' readonly></td>");
				out.println("<td><strong>Editor</strong></td><td><input name='editor' type='text' id='editor' value='"+focr.getEditor()+"' readonly></td>");
				out.println("<td><strong>Continuity</strong></td><td><input name='continuity' type='text' id='continuity' value='"+focr.getContinuity()+"' readonly></td>");
				out.println("</tr></table></td></tr>");
					
				out.println("<tr><td>&nbsp;</td></tr>");
	
				ContinuityInfoType ci = focr.getContinuityInfo();
				 
				int a=1;
				//for each scene
				out.println("<tr><td><table width='800' id='table1'><tbody>");
				out.println("<tr valign='top'><th><strong>CAM</strong></th><th><strong>SOUND</strong></th><th><strong>SCENE</strong></th><th><strong>SLATE</strong></th><th><strong>TAKE</strong></th><th><strong>PRINT</strong></th><th><strong>DURATION</strong></th><th><strong>SHOT DESCRIPTION - COMMENTS <br>(lens, focus, stop, filter)</strong></th></tr>");
				for(SceneInfoType si : ci.getSceneInfo()){
					for(SlateInfoType sl : si.getSlateInfo()){
						for(TakeInfoType ti : sl.getTakeInfo()){
							out.println("<tr valign='top'>");
							out.println("<td align='center'><input name='cam_"+ a+"' type='text' id='cam_"+ a+"' value='" + ti.getCamRoll() + "' size='10' readonly></td>");
							out.println("<td align='center'><input name='sound_"+ a+"' type='text' id='sound_"+ a+"' value='" + ti.getSoundRoll() + "' size='10' readonly></td>");
							out.println("<td align='center'><input name='scene_"+ a+"' type='text' id='scene_"+ a+"' value='" + si.getScene() + "' size='10' readonly></td>");
							out.println("<td align='center'><input name='slate_"+ a+"' type='text' id='slate_"+ a+"' value='" + sl.getSlate() + "' size='10' readonly></td>");
							out.println("<td align='center'><input name='take_"+ a+"' type='text' id='take_"+ a+"' value='" + ti.getTake() + "' size='10' readonly></td>");
							if(ti.isPrint() == true) {
								out.println("<td align='center'><input name='print_"+ a+"' type='checkbox' id='print_"+ a+"' value='true' checked disabled></td>");
							} else {
								out.println("<td align='center'><input name='print_"+ a+"' type='checkbox' id='print_"+ a+"' value='true' disabled></td>");
							}
							out.println("<td align='center'><input name='duration_"+ a+"' type='text' id='duration_"+ a+"' value='" + ti.getDuration() + "' size='10' readonly></td>");
							out.println("<td><textarea name='comments_"+ a+"' cols='50' id='comments_"+ a+"' readonly>" + ti.getComments() + "</textarea></td>");
							out.println("</tr>");
							a++;
						}
					}
				}
				out.println("</tbody></table></td></tr>");
				out.println("<tr><td><input type='button' value='Change to What You Need Here' ></td></tr></table>");
				
				
			}
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
else if(request.getParameter("Submission") != null){

	int count = Integer.parseInt(request.getParameter("count"));
	
	Map<String,SceneInfoType> scenes = new TreeMap<String,SceneInfoType>();
	Map<String,SlateInfoType> slates = new TreeMap<String,SlateInfoType>();
	
	SlateInfoType tempSlate = null;
	String tempSlateNO = null;
	String tempSceneNOSlateNO = null;
	
	SceneInfoType tempScene = null;
	String tempSceneNO = null;
	
	for (int i=1;i<=count;i++){//takes are ordered within each slate. Slates are backwards ordered. Scenes are backwards ordered.
		TakeInfoType ti = new TakeInfoType();
		ti.setTake(new BigInteger(request.getParameter("take_"+i)));
		ti.setDuration(XMLGregorianCalendarImpl.parse(request.getParameter("duration_"+i)));
		
		if (request.getParameter("print_"+i)==null)
			ti.setPrint(false);
		else
			ti.setPrint(true);
		
		ti.setComments(request.getParameter("comments_"+i));
		ti.setCamRoll(request.getParameter("cam_"+i));
		ti.setSoundRoll(new BigInteger(request.getParameter("sound_"+i)));
		
				
		tempSceneNO=request.getParameter("scene_"+i);
		tempSlateNO=request.getParameter("slate_"+i);
		tempSceneNOSlateNO=tempSceneNO+"\t"+tempSlateNO;//concatenation of tempSceneNO and tempSlateNO. The token separator is "\t"
		
		tempSlate=slates.get(tempSceneNOSlateNO);
		if (tempSlate==null){
			SlateInfoType si = new SlateInfoType();
			si.setSlate(new BigInteger(tempSlateNO));
			si.getTakeInfo().add(ti);
			slates.put(tempSceneNOSlateNO, si);//add the newly created slate into the "slates" map
		}
		else{//the slateNO already exists
			tempSlate.getTakeInfo().add(ti);
		}
	}
	for (String key : slates.keySet()){//adds slates to relative scenes
		StringTokenizer st = new StringTokenizer(key);
		tempSceneNO=st.nextToken();
		
		tempScene=scenes.get(tempSceneNO);
		if (tempScene==null){
			SceneInfoType sci = new SceneInfoType();
			sci.setScene(tempSceneNO);
			sci.getSlateInfo().add(slates.get(key));//retrieves the slate associated to the current scene
			scenes.put(tempSceneNO, sci);
		}
		else{//the sceneNO already exists
			tempScene.getSlateInfo().add(slates.get(key));
		}
	}
	List<SceneInfoType> scl = new ArrayList<SceneInfoType>(scenes.values());//creates a list of the scenes and adds it to the continuityInfo facade
	ContinuityInfoType ci = new ContinuityInfoType();
	ci.getSceneInfo().addAll(scl);
	
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.continuityinfo");
		            
	FillOutContinuityReportType focr = new FillOutContinuityReportType();
	focr.setContinuityInfo(ci);
	ObjectFactory of = new ObjectFactory();
	JAXBElement focrElement = of.createFillOutContinuityReport(focr);
	
	Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    //FillOutContinuityReportType focr = (FillOutContinuityReportType)focrElement.getValue();
    File f = new File("./backup/ContinuityReport_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
    m.marshal( focrElement,  f);//output to file
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(focrElement, xmlOS);//out to ByteArray
	String result = xmlOS.toString().replaceAll("ns2:", "");
    
    String workItemID = new String(request.getParameter("workItemID"));
    String sessionHandle = new String(request.getParameter("sessionHandle"));
    String userID = new String(request.getParameter("userID"));
    String submit = new String(request.getParameter("submit"));
  
    session.setAttribute("inputData", result);//to be possibly replaced
    response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/yawlFormServlet?workItemID="+workItemID+"&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit));
    return;
}

else if(request.getParameter("Save") != null){

	int count = Integer.parseInt(request.getParameter("count"));
	
	Map<String,SceneInfoType> scenes = new TreeMap<String,SceneInfoType>();
	Map<String,SlateInfoType> slates = new TreeMap<String,SlateInfoType>();
	
	SlateInfoType tempSlate = null;
	String tempSlateNO = null;
	String tempSceneNOSlateNO = null;
	
	SceneInfoType tempScene = null;
	String tempSceneNO = null;
	
	for (int i=1;i<=count;i++){//takes are ordered within each slate. Slates are backwards ordered. Scenes are backwards ordered.
		TakeInfoType ti = new TakeInfoType();
		ti.setTake(new BigInteger(request.getParameter("take_"+i)));
		ti.setDuration(XMLGregorianCalendarImpl.parse(request.getParameter("duration_"+i)));
		if (request.getParameter("print_"+i)==null)
			ti.setPrint(false);
		else
			ti.setPrint(true);
		ti.setComments(request.getParameter("comments_"+i));
		ti.setCamRoll(request.getParameter("cam_"+i));
		ti.setSoundRoll(new BigInteger(request.getParameter("sound_"+i)));
		
				
		tempSceneNO=request.getParameter("scene_"+i);
		tempSlateNO=request.getParameter("slate_"+i);
		tempSceneNOSlateNO=tempSceneNO+"\t"+tempSlateNO;//concatenation of tempSceneNO and tempSlateNO. The token separator is "\t"
		
		tempSlate=slates.get(tempSceneNOSlateNO);
		if (tempSlate==null){
			SlateInfoType si = new SlateInfoType();
			si.setSlate(new BigInteger(tempSlateNO));
			si.getTakeInfo().add(ti);
			slates.put(tempSceneNOSlateNO, si);//add the newly created slate into the "slates" map
		}
		else{//the slateNO already exists
			tempSlate.getTakeInfo().add(ti);
		}
	}
	for (String key : slates.keySet()){//adds slates to relative scenes
		StringTokenizer st = new StringTokenizer(key);
		tempSceneNO=st.nextToken();
		
		tempScene=scenes.get(tempSceneNO);
		if (tempScene==null){
			SceneInfoType sci = new SceneInfoType();
			sci.setScene(tempSceneNO);
			sci.getSlateInfo().add(slates.get(key));//retrieves the slate associated to the current scene
			scenes.put(tempSceneNO, sci);
		}
		else{//the sceneNO already exists
			tempScene.getSlateInfo().add(slates.get(key));
		}
	}
	List<SceneInfoType> scl = new ArrayList<SceneInfoType>(scenes.values());//creates a list of the scenes and adds it to the continuityInfo facade
	ContinuityInfoType ci = new ContinuityInfoType();
	ci.getSceneInfo().addAll(scl);
	
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.continuityinfo");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement focrElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
	FillOutContinuityReportType focr = (FillOutContinuityReportType)focrElement.getValue();
	
	focr.setContinuityInfo(ci);
	
	Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(focrElement, xmlOS);//out to ByteArray

    response.setHeader("Content-Disposition", "attachment;filename=\"ContinuityReport_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
    response.setHeader("Content-Type", "text/xml");

    ServletOutputStream outs = response.getOutputStream();
    xmlOS.writeTo(outs);
    outs.close();
}
%>
</body>
</html>