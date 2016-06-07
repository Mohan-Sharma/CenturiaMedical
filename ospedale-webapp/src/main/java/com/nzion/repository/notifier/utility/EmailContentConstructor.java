package com.nzion.repository.notifier.utility;

import com.nzion.domain.*;
import com.nzion.view.PatientViewObject;
import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Mohan Sharma on 5/29/2015.
 */
public class EmailContentConstructor {

    public static String getBodyAppointmentConfirmationEmail(Schedule schedule, Patient patient, Person provider, Map<String, Object> clinicDetails) {
        String clinicName = null;
        String where = null;
        if(!clinicDetails.isEmpty()) {
            clinicName = (String)clinicDetails.get("clinic_name");
            where = constructWhere(clinicDetails);

        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        buffer.append("<head>");
        buffer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />");
        buffer.append("<title>Email</title>");
        buffer.append("</head>");
        buffer.append("<body style=\"margin:0px; padding:0px;\">");
        buffer.append("<table width=\"100%\" border=\"0\" cellpadding=\"20\" style=\"background-color:#F4F4F4;\">");
        buffer.append("<tr>");
        buffer.append("<td><table width=\"650\" border=\"0\" cellpadding=\"0\" align=\"center\" style=\"background-color:#FFFFFF; border:solid 1px #D8D8D8; border-top:solid 8px #99CC33;\">");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:18px; margin:20px; margin-bottom:5px; color:#81AA2B;\"><strong>Your Appointment Confirmation</strong></p>");
        buffer.append("<p style=\"margin:20px; font-family:Arial, Helvetica, sans-serif; font-size:22px; margin-top:5px; \">"+constructDate(schedule.getStartTime(), schedule.getStartDate())+" at "+constructTime(schedule.getStartTime(), schedule.getStartDate())+"</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\">");
        buffer.append("<table width=\"600\" border=\"0\" cellpadding=\"5\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px;\">Dear "+constructName(patient)+",</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Your appointment has been confirmed by the doctor. <br />");
        buffer.append("Following are the details for your reference</p>");
        buffer.append("<table width=\"500\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td width=\"120\" style=\"border-bottom:solid 1px #CCCCCC; border-top:solid 5px #D1D1D1; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Doctor's Name: </strong></p> </td>");
        buffer.append("<td width=\"334\" style=\"border-bottom:solid 1px #CCCCCC; border-top:solid 5px #D1D1D1; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+constructName(provider)+"</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Clinic</strong></p>                </td>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+clinicName+"</p>                </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong> Where </strong></p></td>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+where+"<br />");
        //buffer.append("Kuwait<br />");
        //buffer.append("Ph: +965 2227 5900<br />");
        buffer.append("</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 5px #D1D1D1;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong> Reason </strong></p> </td>");
        buffer.append("<td style=\"border-bottom:solid 5px #D1D1D1;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+constructReason(schedule)+"</p> </td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">&nbsp;</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Kind Regards, </p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Customer Care Team</strong><br />");
        buffer.append(getClinicName(clinicDetails)+"<br>Contact "+getClinicName(clinicDetails)+" on Ph : "+getMobileNumber(clinicDetails)+"</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">");
        //buffer.append("Ph:");
        //buffer.append("+965 2227 5900 or <br />");
        buffer.append("</p>\t</td>");
        buffer.append("</tr>");
        buffer.append("</table>\t\t   </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td width=\"246\" style=\"border-bottom:solid 5px #99CC33;\">&nbsp;</td>");
        buffer.append("<td width=\"282\" rowspan=\"2\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:16px; font-style:italic; font-weight:bold; color:#99CC33; text-align:center;\">Building a Smart Care Community</p>          </td>");
        buffer.append("<td width=\"112\" style=\"border-bottom:solid 5px #99CC33;\">&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td width=\"246\">&nbsp;</td>");
        buffer.append("<td>&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\"><table width=\"595\" border=\"0\" cellpadding=\"5\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td>&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:12px; line-height:20px; text-align:center;\"><strong>&copy; 2015, afyaarabia.com &ndash; All Rights  Reserved</strong></p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; line-height:16px; color:#999999;\">Disclaimer:");
        buffer.append("This notification is not designed to and does not provide medical advice, professional diagnosis, opinion, treatment but is a mere facilitator for medical related services to you. Through this notification and linkages to other sites, Afyaarabia provides general information for facilitation of services requested only. Afyaarabia is not liable or responsible for any advice, course of treatment, diagnosis or any other information, services or product you obtain through this notification.</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; line-height:16px; color:#999999;\">&nbsp;</p></td>");
        buffer.append("</tr>");
        buffer.append("</table>          </td>");
        buffer.append("</tr>");
        buffer.append("</table></td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</body>");
        buffer.append("</html>");
        return buffer.toString();
    }

    public static String getBodyAppointmentRescheduledEmail(Schedule schedule, Patient patient, Person provider, Map<String, Object> clinicDetails) {
        String clinicName = null;
        String where = null;
        if(!clinicDetails.isEmpty()) {
            clinicName = (String)clinicDetails.get("clinic_name");
            where = constructWhere(clinicDetails);

        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        buffer.append("<head>");
        buffer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />");
        buffer.append("<title>Email</title>");
        buffer.append("</head>");
        buffer.append("<body style=\"margin:0px; padding:0px;\">");
        buffer.append("<table width=\"100%\" border=\"0\" cellpadding=\"20\" style=\"background-color:#F4F4F4;\">");
        buffer.append("<tr>");
        buffer.append("<td><table width=\"650\" border=\"0\" cellpadding=\"0\" align=\"center\" style=\"background-color:#FFFFFF; border:solid 1px #D8D8D8; border-top:solid 8px #99CC33;\">");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:18px; margin:20px; margin-bottom:5px; color:#81AA2B;\"><strong>Appointment Rescheduled Information</strong></p>");
        buffer.append("<p style=\"margin:20px; font-family:Arial, Helvetica, sans-serif; font-size:22px; margin-top:5px; \">"+constructDate(schedule.getStartTime(), schedule.getStartDate())+" at "+constructTime(schedule.getStartTime(), schedule.getStartDate())+"</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\">");
        buffer.append("<table width=\"600\" border=\"0\" cellpadding=\"5\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px;\">Dear "+constructName(patient)+",</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Your appointment has been rescheduled. <br />");
        buffer.append("Following are the details for your reference</p>");
        buffer.append("<table width=\"500\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td width=\"120\" style=\"border-bottom:solid 1px #CCCCCC; border-top:solid 5px #D1D1D1; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Doctor's Name: </strong></p> </td>");
        buffer.append("<td width=\"334\" style=\"border-bottom:solid 1px #CCCCCC; border-top:solid 5px #D1D1D1; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+constructName(provider)+"</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Clinic</strong></p>                </td>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+clinicName+"</p>                </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong> Where </strong></p></td>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+where+"<br />");
        //buffer.append("Kuwait<br />");
        //buffer.append("Ph: +965 2227 5900<br />");
        buffer.append("</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 5px #D1D1D1;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong> Reason </strong></p> </td>");
        buffer.append("<td style=\"border-bottom:solid 5px #D1D1D1;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+constructReason(schedule)+"</p> </td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">&nbsp;</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Kind Regards, </p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Customer Care Team</strong><br />");
        buffer.append(getClinicName(clinicDetails)+"<br>Contact "+getClinicName(clinicDetails)+" on Ph : "+getMobileNumber(clinicDetails)+"</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">");
        //buffer.append("Ph:");
        //buffer.append("+965 2227 5900 or <br />");
        buffer.append("</p>\t</td>");
        buffer.append("</tr>");
        buffer.append("</table>\t\t   </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td width=\"246\" style=\"border-bottom:solid 5px #99CC33;\">&nbsp;</td>");
        buffer.append("<td width=\"282\" rowspan=\"2\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:16px; font-style:italic; font-weight:bold; color:#99CC33; text-align:center;\">Building a Smart Care Community</p>          </td>");
        buffer.append("<td width=\"112\" style=\"border-bottom:solid 5px #99CC33;\">&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td width=\"246\">&nbsp;</td>");
        buffer.append("<td>&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\"><table width=\"595\" border=\"0\" cellpadding=\"5\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td>&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:12px; line-height:20px; text-align:center;\"><strong>&copy; 2015, afyaarabia.com &ndash; All Rights  Reserved</strong><br />");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; line-height:16px; color:#999999;\">Disclaimer:");
        buffer.append("This notification is not designed to and does not provide medical advice, professional diagnosis, opinion, treatment but is a mere facilitator for medical related services to you. Through this notification and linkages to other sites, Afyaarabia provides general information for facilitation of services requested only. Afyaarabia is not liable or responsible for any advice, course of treatment, diagnosis or any other information, services or product you obtain through this notification.</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; line-height:16px; color:#999999;\">&nbsp;</p></td>");
        buffer.append("</tr>");
        buffer.append("</table>          </td>");
        buffer.append("</tr>");
        buffer.append("</table></td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</body>");
        buffer.append("</html>");
        return buffer.toString();
    }

    public static String getBodyAppointmentCancelledMail(Schedule schedule, Patient patient, Person provider, Map<String, Object> clinicDetails) {
        String clinicName = null;
        String where = null;
        if(!clinicDetails.isEmpty()) {
            clinicName = (String)clinicDetails.get("clinic_name");
            where = constructWhere(clinicDetails);

        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        buffer.append("<head>");
        buffer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />");
        buffer.append("<title>Email</title>");
        buffer.append("</head>");
        buffer.append("<body style=\"margin:0px; padding:0px;\">");
        buffer.append("<table width=\"100%\" border=\"0\" cellpadding=\"20\" style=\"background-color:#F4F4F4;\">");
        buffer.append("<tr>");
        buffer.append("<td><table width=\"650\" border=\"0\" cellpadding=\"0\" align=\"center\" style=\"background-color:#FFFFFF; border:solid 1px #D8D8D8; border-top:solid 8px #99CC33;\">");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:18px; margin:20px; margin-bottom:5px; color:#81AA2B;\"><strong>Appointment Cancelled</strong></p>");
        buffer.append("<p style=\"margin:20px; font-family:Arial, Helvetica, sans-serif; font-size:22px; margin-top:5px; \">"+constructDate(schedule.getStartTime(), schedule.getStartDate())+" at "+constructTime(schedule.getStartTime(), schedule.getStartDate())+"</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\">");
        buffer.append("<table width=\"600\" border=\"0\" cellpadding=\"5\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px;\">Dear "+constructName(patient)+",</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Your appointment has been cancelled<br />");
        buffer.append("Following are the details for your reference</p>");
        buffer.append("<table width=\"500\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td width=\"120\" style=\"border-bottom:solid 1px #CCCCCC; border-top:solid 5px #D1D1D1; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Doctor's Name: </strong></p> </td>");
        buffer.append("<td width=\"334\" style=\"border-bottom:solid 1px #CCCCCC; border-top:solid 5px #D1D1D1; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+constructName(provider)+"</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Clinic</strong></p>                </td>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+clinicName+"</p>                </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong> Where </strong></p></td>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+where+"<br />");
        //buffer.append("Kuwait<br />");
        //buffer.append("Ph: +965 2227 5900<br />");
        buffer.append("</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 5px #D1D1D1;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong> Reason </strong></p> </td>");
        buffer.append("<td style=\"border-bottom:solid 5px #D1D1D1;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+constructCancelReason(schedule)+"</p> </td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">&nbsp;</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Kind Regards, </p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Customer Care Team</strong><br />");
        buffer.append(getClinicName(clinicDetails)+"<br>Contact "+getClinicName(clinicDetails)+" on Ph : "+getMobileNumber(clinicDetails)+"</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">");
        //buffer.append("Ph:");
        //buffer.append("+965 2227 5900 or <br />");
        buffer.append("</p>\t</td>");
        buffer.append("</tr>");
        buffer.append("</table>\t\t   </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td width=\"246\" style=\"border-bottom:solid 5px #99CC33;\">&nbsp;</td>");
        buffer.append("<td width=\"282\" rowspan=\"2\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:16px; font-style:italic; font-weight:bold; color:#99CC33; text-align:center;\">Building a Smart Care Community</p>          </td>");
        buffer.append("<td width=\"112\" style=\"border-bottom:solid 5px #99CC33;\">&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td width=\"246\">&nbsp;</td>");
        buffer.append("<td>&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\"><table width=\"595\" border=\"0\" cellpadding=\"5\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td>&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:12px; line-height:20px; text-align:center;\"><strong>&copy; 2015, afyaarabia.com &ndash; All Rights  Reserved</strong><br /></p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; line-height:16px; color:#999999;\">Disclaimer:");
        buffer.append("This notification is not designed to and does not provide medical advice, professional diagnosis, opinion, treatment but is a mere facilitator for medical related services to you. Through this notification and linkages to other sites, Afyaarabia provides general information for facilitation of services requested only. Afyaarabia is not liable or responsible for any advice, course of treatment, diagnosis or any other information, services or product you obtain through this notification.</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; line-height:16px; color:#999999;\">&nbsp;</p></td>");
        buffer.append("</tr>");
        buffer.append("</table>          </td>");
        buffer.append("</tr>");
        buffer.append("</table></td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</body>");
        buffer.append("</html>");
        return buffer.toString();
    }

    public static String constructName(Object object){
        StringBuilder stringBuilder = new StringBuilder();
        String name=null;
        if(object instanceof Patient) {
            Patient patient = (Patient)object;
            if(patient.getSalutation() != null)
                stringBuilder.append(patient.getSalutation()+". ");
            if(patient.getFirstName() != null)
                stringBuilder.append(patient.getFirstName()+" ");
            if(patient.getLastName() != null)
                stringBuilder.append(patient.getLastName());
            name = stringBuilder.toString();
        }

        if(object instanceof Provider) {
            Provider provider = (Provider)object;
            if(provider.getSalutation() != null)
                stringBuilder.append(provider.getSalutation()+". ");
            if(provider.getFirstName() != null)
                stringBuilder.append(provider.getFirstName()+" ");
            if(provider.getLastName() != null)
                stringBuilder.append(provider.getLastName());
            name =  stringBuilder.toString();
        }
        return name;
    }

    public static String constructWhere(Map<String, Object> clinicDetails){
        StringBuilder stringBuilder = new StringBuilder();
        if(clinicDetails.get("address") != null)
            stringBuilder.append((String)clinicDetails.get("address")+System.getProperty("line.separator"));
        if(clinicDetails.get("additional_address") != null)
            stringBuilder.append((String)clinicDetails.get("additional_address")+System.getProperty("line.separator"));
        if(clinicDetails.get("state") != null)
            stringBuilder.append((String)clinicDetails.get("state")+System.getProperty("line.separator"));
        if(clinicDetails.get("country") != null)
            stringBuilder.append((String)clinicDetails.get("country")+System.getProperty("line.separator"));
        if(clinicDetails.get("office_phone_number") != null)
            stringBuilder.append("Ph: "+(String)clinicDetails.get("office_phone_number"));
        return stringBuilder.toString();
    }

    private static String constructDate(Date date, Date scheduleDate){
        LocalDate localDate = new LocalDate(scheduleDate);
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, localDate.getYear());
        calendar.set(Calendar.MONTH, localDate.getMonthOfYear()-1);
        calendar.set(Calendar.DAY_OF_MONTH, localDate.getDayOfMonth());
        Date furnishedDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd, yyyy");
        return dateFormat.format(furnishedDate);
    }

    private static String constructTime(Date date, Date scheduleDate){
        LocalDate localDate = new LocalDate(scheduleDate);
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, localDate.getYear());
        calendar.set(Calendar.MONTH, localDate.getMonthOfYear());
        calendar.set(Calendar.DAY_OF_MONTH, localDate.getDayOfMonth());
        Date furnishedDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
        return dateFormat.format(furnishedDate);
    }

    private static String constructReason(Schedule schedule){
        return schedule.getComments() != null ? schedule.getComments() : "";
    }

    private static String constructCancelReason(Schedule schedule){
        return schedule.getCancelReason() != null ? schedule.getCancelReason() : "";
    }

    public static String setBodyNotificationMail(Patient patient, Person provider) {
        StringBuffer body
                = new StringBuffer("<html><body style =\"font-size: 12\">");
        body.append("<p style=\"font-size:12px;color:black;font-family:Microsoft Sans Serif;\">Dear, "+ EmailContentConstructor.constructName(patient)+",</p>");
        body.append("<p style=\"font-size:12px;color:black;font-family:Microsoft Sans Serif;margin-left:20px;\">This is a reminder email for your followup appointment tomorrow with "+ EmailContentConstructor.constructName(provider)+". Please book an appointment at the earliest.</p>");
        body.append("<p style=\"font-size:12px;color:black;font-family:Microsoft Sans Serif;\">Regards,<br>Receptionists, Centuria.</p>");
        return body.toString();
    }

    public static String setSubjectForConsolidatedAttendanceRegisterAttachedMail(){
        return "Followup Notification";
    }

    public static String setSubjectForPatientAppointment(){
        return "Patient Appointment Notification";
    }

    public static String setSubjectOfAppointmentRescheduledMail() {
        return "Appointment Rescheduled";
    }

    public static String setSubjectForPatientAppointmentCancelledMail(){
        return "Appointment Cancelled";
    }

    public static String setSubjectOfAppointmentReminderMail() {
        return "Appointment Reminder";
    }

    public static String setSubjectOfTeleConsultationAppointmentReminderMail() {
        return "Tele Consultation Appointment Not Confirmed";
    }

    public static String setBodyNotificationMailToProviderForHighPriorityPatient(Schedule schedule, Patient patient, Person provider, Map<String, Object> clinicDetails) {
        StringBuilder stringBuilder = new StringBuilder();
        String clinicName = null;
        String where = null;
        if(!clinicDetails.isEmpty()) {
            clinicName = (String)clinicDetails.get("clinic_name");
            where = constructWhere(clinicDetails);

        }
        stringBuilder.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        stringBuilder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        stringBuilder.append("<head>");
        stringBuilder.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />");
        stringBuilder.append("<title>Email</title>");
        stringBuilder.append("</head>");
        stringBuilder.append("<body style=\"margin:0px; padding:0px;\">");
        stringBuilder.append("<table width=\"100%\" border=\"0\" cellpadding=\"20\" style=\"background-color:#F4F4F4;\">");
        stringBuilder.append("<tr>");
        stringBuilder.append("<td><table width=\"650\" border=\"0\" cellpadding=\"0\" align=\"center\" style=\"background-color:#FFFFFF; border:solid 1px #D8D8D8; border-top:solid 8px #99CC33;\">");
        stringBuilder.append("<tr>");
        stringBuilder.append("<td colspan=\"3\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:18px; margin:20px; margin-bottom:5px; color:#81AA2B;\"><strong>High Priority Appointment Notification</strong></p>");
        stringBuilder.append("<p style=\"margin:20px; font-family:Arial, Helvetica, sans-serif; font-size:22px; margin-top:5px; \">"+constructDate(schedule.getStartTime(), schedule.getStartDate())+" at "+constructTime(schedule.getStartTime(), schedule.getStartDate())+"</p></td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("<tr>");
        stringBuilder.append("<td colspan=\"3\">");
        stringBuilder.append("<table width=\"600\" border=\"0\" cellpadding=\"5\" align=\"center\">");
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px;\">Dear "+constructName(provider)+",</p>");
        stringBuilder.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">An appointment has been confirmed with a high priority patient. <br />");
        stringBuilder.append("Following are the details for your reference</p>");
        stringBuilder.append("<table width=\"500\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\" align=\"center\">");
        stringBuilder.append("<tr>");
        stringBuilder.append("<td width=\"120\" style=\"border-bottom:solid 1px #CCCCCC; border-top:solid 5px #D1D1D1; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Patient's Name: </strong></p> </td>");
        stringBuilder.append("<td width=\"334\" style=\"border-bottom:solid 1px #CCCCCC; border-top:solid 5px #D1D1D1; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+constructName(patient)+"</p></td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("<tr>");
        stringBuilder.append("<td style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Clinic</strong></p>                </td>");
        stringBuilder.append("<td style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+clinicName+"</p>                </td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("<tr>");
        stringBuilder.append("<td style=\"border-bottom:solid 1px #CCCCCC; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong> Where </strong></p></td>");
        stringBuilder.append("<td style=\"border-bottom:solid 1px #CCCCCC; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+where+"<br />");
        //stringBuilder.append("Kuwait<br />");
        //stringBuilder.append("Ph: +965 2227 5900<br />");
        stringBuilder.append("</p></td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("<tr>");
        stringBuilder.append("<td style=\"border-bottom:solid 5px #D1D1D1;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong> Reason </strong></p> </td>");
        stringBuilder.append("<td style=\"border-bottom:solid 5px #D1D1D1;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+constructReason(schedule)+"</p> </td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table>");
        stringBuilder.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">&nbsp;</p>");
        stringBuilder.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Kind Regards, </p>");
        stringBuilder.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Customer Care Team</strong><br />");
        stringBuilder.append(getClinicName(clinicDetails)+"<br>Contact "+getClinicName(clinicDetails)+" on Ph : "+getMobileNumber(clinicDetails)+"</p>");
        stringBuilder.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">");
        //stringBuilder.append("Ph:");
        //stringBuilder.append("+965 2227 5900 or <br />");
        stringBuilder.append("</p>\t</td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table>\t\t   </td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("<tr>");
        stringBuilder.append("<td width=\"246\" style=\"border-bottom:solid 5px #99CC33;\">&nbsp;</td>");
        stringBuilder.append("<td width=\"282\" rowspan=\"2\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:16px; font-style:italic; font-weight:bold; color:#99CC33; text-align:center;\">Building a Smart Care Community</p>          </td>");
        stringBuilder.append("<td width=\"112\" style=\"border-bottom:solid 5px #99CC33;\">&nbsp;</td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("<tr>");
        stringBuilder.append("<td width=\"246\">&nbsp;</td>");
        stringBuilder.append("<td>&nbsp;</td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("<tr>");
        stringBuilder.append("<td colspan=\"3\"><table width=\"595\" border=\"0\" cellpadding=\"5\" align=\"center\">");
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>&nbsp;</td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("<tr>");
        stringBuilder.append("<td>");
        stringBuilder.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:12px; line-height:20px; text-align:center;\"><strong>&copy; 2015, afyaarabia.com &ndash; All Rights  Reserved</strong><br /></p>");
        stringBuilder.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; line-height:16px; color:#999999;\">Disclaimer:");
        stringBuilder.append("This notification is not designed to and does not provide medical advice, professional diagnosis, opinion, treatment but is a mere facilitator for medical related services to you. Through this notification and linkages to other sites, Afyaarabia provides general information for facilitation of services requested only. Afyaarabia is not liable or responsible for any advice, course of treatment, diagnosis or any other information, services or product you obtain through this notification.</p>");
        stringBuilder.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; line-height:16px; color:#999999;\">&nbsp;</p></td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table>          </td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table></td>");
        stringBuilder.append("</tr>");
        stringBuilder.append("</table>");
        stringBuilder.append("</body>");
        stringBuilder.append("</html>");
        return stringBuilder.toString();
    }

    public static String getBodyOfAppointmentReminderMail(Schedule schedule, Patient patient, Person provider, Map<String, Object> clinicDetails) {
        String clinicName = null;
        String where = null;
        if(!clinicDetails.isEmpty()) {
            clinicName = (String)clinicDetails.get("clinic_name");
            where = constructWhere(clinicDetails);

        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        buffer.append("<head>");
        buffer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />");
        buffer.append("<title>Email</title>");
        buffer.append("</head>");
        buffer.append("<body style=\"margin:0px; padding:0px;\">");
        buffer.append("<table width=\"100%\" border=\"0\" cellpadding=\"20\" style=\"background-color:#F4F4F4;\">");
        buffer.append("<tr>");
        buffer.append("<td><table width=\"650\" border=\"0\" cellpadding=\"0\" align=\"center\" style=\"background-color:#FFFFFF; border:solid 1px #D8D8D8; border-top:solid 8px #99CC33;\">");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:18px; margin:20px; margin-bottom:5px; color:#81AA2B;\"><strong>Appointment Reminder</strong></p>");
        buffer.append("<p style=\"margin:20px; font-family:Arial, Helvetica, sans-serif; font-size:22px; margin-top:5px; \">"+constructDate(schedule.getStartTime(), schedule.getStartDate())+" at "+constructTime(schedule.getStartTime(), schedule.getStartDate())+"</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\">");
        buffer.append("<table width=\"600\" border=\"0\" cellpadding=\"5\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px;\">Dear "+constructName(patient)+",</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">It is a reminder email for your appointment.<br />");
        buffer.append("Following are the details for your reference</p>");
        buffer.append("<table width=\"500\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td width=\"120\" style=\"border-bottom:solid 1px #CCCCCC; border-top:solid 5px #D1D1D1; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Doctor's Name: </strong></p> </td>");
        buffer.append("<td width=\"334\" style=\"border-bottom:solid 1px #CCCCCC; border-top:solid 5px #D1D1D1; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+constructName(provider)+"</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Clinic</strong></p>                </td>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+clinicName+"</p>                </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong> Where </strong></p></td>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+where+"<br />");
        //buffer.append("Kuwait<br />");
        //buffer.append("Ph: +965 2227 5900<br />");
        buffer.append("</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 5px #D1D1D1;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong> Reason </strong></p> </td>");
        buffer.append("<td style=\"border-bottom:solid 5px #D1D1D1;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+constructReason(schedule)+"</p> </td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">&nbsp;</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Kind Regards, </p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Customer Care Team</strong><br />");
        buffer.append(getClinicName(clinicDetails)+"<br>Contact "+getClinicName(clinicDetails)+" on Ph : "+getMobileNumber(clinicDetails)+"</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">");
        //buffer.append("Ph:");
        //buffer.append("+965 2227 5900 or <br />");
        buffer.append("</p>\t</td>");
        buffer.append("</tr>");
        buffer.append("</table>\t\t   </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td width=\"246\" style=\"border-bottom:solid 5px #99CC33;\">&nbsp;</td>");
        buffer.append("<td width=\"282\" rowspan=\"2\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:16px; font-style:italic; font-weight:bold; color:#99CC33; text-align:center;\">Building a Smart Care Community</p>          </td>");
        buffer.append("<td width=\"112\" style=\"border-bottom:solid 5px #99CC33;\">&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td width=\"246\">&nbsp;</td>");
        buffer.append("<td>&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\"><table width=\"595\" border=\"0\" cellpadding=\"5\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td width=\"519\" style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"margin:0px; text-align:center; font-family:Arial, Helvetica, sans-serif; font-size:14px;\"> <a style=\"color:#3399CC; text-decoration:none;\" href=\"http://www.afyaarabia.com/\" target=\"_blank\">www.afyaarabia.com</a> </p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td><p style=\"margin:0px; text-align:center; font-family:Arial, Helvetica, sans-serif; font-size:14px;\"> <a style=\"color:#3399CC; text-decoration:none;\" href=\"#\">Customer Support</a> | <a style=\"color:#3399CC; text-decoration:none;\" href=\"#\">Download App</a> | <a style=\"color:#3399CC; text-decoration:none;\" href=\"#\">Feedback</a> | <a style=\"color:#3399CC; text-decoration:none;\" href=\"#\">Contact Us </a></p> </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td>&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:12px; line-height:20px; text-align:center;\"><strong>&copy; 2015, afyaarabia.com &ndash; All Rights  Reserved</strong><br /></p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; line-height:16px; color:#999999;\">Disclaimer:");
        buffer.append("This notification is not designed to and does not provide medical advice, professional diagnosis, opinion, treatment but is a mere facilitator for medical related services to you. Through this notification and linkages to other sites, Afyaarabia provides general information for facilitation of services requested only. Afyaarabia is not liable or responsible for any advice, course of treatment, diagnosis or any other information, services or product you obtain through this notification.</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; line-height:16px; color:#999999;\">&nbsp;</p></td>");
        buffer.append("</tr>");
        buffer.append("</table>          </td>");
        buffer.append("</tr>");
        buffer.append("</table></td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</body>");
        buffer.append("</html>");
        return buffer.toString();
    }

    public static String getBodyTeleConsultationAppointmentNonConfirmationEmail(Schedule schedule, Patient patient, Person provider, Map<String, Object> clinicDetails) {
        String clinicName = null;
        String where = null;
        if(!clinicDetails.isEmpty()) {
            clinicName = (String)clinicDetails.get("clinic_name");
            where = constructWhere(clinicDetails);

        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        buffer.append("<head>");
        buffer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />");
        buffer.append("<title>Email</title>");
        buffer.append("</head>");
        buffer.append("<body style=\"margin:0px; padding:0px;\">");
        buffer.append("<table width=\"100%\" border=\"0\" cellpadding=\"20\" style=\"background-color:#F4F4F4;\">");
        buffer.append("<tr>");
        buffer.append("<td><table width=\"650\" border=\"0\" cellpadding=\"0\" align=\"center\" style=\"background-color:#FFFFFF; border:solid 1px #D8D8D8; border-top:solid 8px #99CC33;\">");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:18px; margin:20px; margin-bottom:5px; color:#81AA2B;\"><strong>Your Tele Consultation Appointment Not Confirmed</strong></p>");
        buffer.append("<p style=\"margin:20px; font-family:Arial, Helvetica, sans-serif; font-size:22px; margin-top:5px; \">"+constructDate(schedule.getStartTime(), schedule.getStartDate())+" at "+constructTime(schedule.getStartTime(), schedule.getStartDate())+"</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\">");
        buffer.append("<table width=\"600\" border=\"0\" cellpadding=\"5\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px;\">Dear "+constructName(patient)+",</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Your tele-consultation appointment has not been confirmed. <br />");
        buffer.append("Following are the details for your reference</p>");
        buffer.append("<table width=\"500\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td width=\"120\" style=\"border-bottom:solid 1px #CCCCCC; border-top:solid 5px #D1D1D1; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Doctor's Name: </strong></p> </td>");
        buffer.append("<td width=\"334\" style=\"border-bottom:solid 1px #CCCCCC; border-top:solid 5px #D1D1D1; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+constructName(provider)+"</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Clinic</strong></p>                </td>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+clinicName+"</p>                </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong> Where </strong></p></td>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+where+"<br />");
        //buffer.append("Kuwait<br />");
        //buffer.append("Ph: +965 2227 5900<br />");
        buffer.append("</p></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 5px #D1D1D1;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong> Reason </strong></p> </td>");
        buffer.append("<td style=\"border-bottom:solid 5px #D1D1D1;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">"+constructReason(schedule)+"</p> </td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">&nbsp;</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Kind Regards, </p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Customer Care Team</strong><br />");
        buffer.append(getClinicName(clinicDetails)+"<br>Contact "+getClinicName(clinicDetails)+" on Ph : "+getMobileNumber(clinicDetails)+"</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">");
        //buffer.append("Ph:");
        //buffer.append("+965 2227 5900 or <br />");
        buffer.append("</p>\t</td>");
        buffer.append("</tr>");
        buffer.append("</table>\t\t   </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td width=\"246\" style=\"border-bottom:solid 5px #99CC33;\">&nbsp;</td>");
        buffer.append("<td width=\"282\" rowspan=\"2\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:16px; font-style:italic; font-weight:bold; color:#99CC33; text-align:center;\">Building a Smart Care Community</p>          </td>");
        buffer.append("<td width=\"112\" style=\"border-bottom:solid 5px #99CC33;\">&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td width=\"246\">&nbsp;</td>");
        buffer.append("<td>&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\"><table width=\"595\" border=\"0\" cellpadding=\"5\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td>&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:12px; line-height:20px; text-align:center;\"><strong>&copy; 2015, afyaarabia.com &ndash; All Rights  Reserved</strong><br /></p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; line-height:16px; color:#999999;\">Disclaimer:");
        buffer.append("This notification is not designed to and does not provide medical advice, professional diagnosis, opinion, treatment but is a mere facilitator for medical related services to you. Through this notification and linkages to other sites, Afyaarabia provides general information for facilitation of services requested only. Afyaarabia is not liable or responsible for any advice, course of treatment, diagnosis or any other information, services or product you obtain through this notification.</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; line-height:16px; color:#999999;\">&nbsp;</p></td>");
        buffer.append("</tr>");
        buffer.append("</table>          </td>");
        buffer.append("</tr>");
        buffer.append("</table></td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</body>");
        buffer.append("</html>");
        return buffer.toString();
    }

    public static String setSubjectForHighPriorityAppointment() {
        return "High Priority Appointment Notification";
    }

    private static String getClinicName(Map<String, Object> clinicDetails){
        String clinicName = "";
        if(!clinicDetails.isEmpty()) {
            clinicName = (String)clinicDetails.get("clinic_name");
        }
        return clinicName;
    }

    private static String getMobileNumber(Map<String, Object> clinicDetails){
        String mobileNumber = "";
        if(clinicDetails.get("office_phone_number") != null)
            mobileNumber = (String)clinicDetails.get("office_phone_number");
        return mobileNumber;
    }

    private static String getEmailId(Map<String, Object> clinicDetails){
        String emailId = "";
        if(!clinicDetails.isEmpty()) {
            emailId = (String)clinicDetails.get("email_id");
        }
        return emailId;
    }
    public static String getBodyForDoctorRegistration(UserLogin user) {
    /*    String clinicName = null;
        String where = null;
        if(!clinicDetails.isEmpty()) {
            clinicName = (String)clinicDetails.get("clinic_name");
            where = constructWhere(clinicDetails);

        }*/
        StringBuffer buffer = new StringBuffer();
        buffer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        buffer.append("<head>");
        buffer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />");
        /*buffer.append("<title>Afya - Doctor Creation Notification Email</title>");*/
        buffer.append("</head>");
        buffer.append("<body style=\"margin:0px; padding:0px;\">");
        buffer.append("<table width=\"100%\" border=\"0\" cellpadding=\"20\" style=\"background-color:#F4F4F4;\">");
        buffer.append("<tr><td><table width=\"650\" border=\"0\" cellpadding=\"0\" align=\"center\" style=\"background-color:#FFFFFF; border:solid 1px #D8D8D8; border-top:solid 8px #99CC33;\">");
        buffer.append("<tr><td><table width=\"600\" border=\"0\" cellpadding=\"5\" align=\"center\"><tr><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:3px;\">Respected	<strong>"+user.getPerson().getFirstName() + " " +user.getPerson().getLastName() + "</strong>,<br/><br/>Welcome to Afya ecosystem. Your user-name is " + user.getUsername() + ".<br/>" +"Password is " + user.getPassword() + ". You are requested to change password during your first log in.<br/><br/>" +
                "You can login to Afya link: <a style=\"color:#3399CC; text-decoration:none;\" href=\"https://www.afyaarabia.com/afya-portal/login\">https://www.afyaarabia.com/afya-portal/login</a><br/><br/>" +
                "Best Regards,<br/>Afya Community Care</p></tr></table></td></tr>");
        buffer.append("<tr><td><table width=\"600\" border=\"0\" cellpadding=\"5\" align=\"center\"><tr><td width=\"150\" style=\"border-bottom:solid 5px #99CC33;\">&nbsp;</td><td width=\"330\" rowspan=\"2\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:16px; font-style:italic; font-weight:bold; color:#99CC33; text-align:center;\">Building a Smart Care Community</p></td>");
        buffer.append("&nbsp;<td width=\"100\" style=\"border-bottom:solid 5px #99CC33;\">&nbsp;</td></tr>");
        buffer.append("<tr><td width=\"246\">&nbsp;</td><td>&nbsp;</td></tr></table>");
        buffer.append("<tr>\n" +
                "                <td colspan=\"3\"><table width=\"595\" border=\"0\" cellpadding=\"5\" align=\"center\">\n" +
                "                    <tr>");
        buffer.append("<td width=\"519\" style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"margin:0px; text-align:center; font-family:Arial, Helvetica, sans-serif; font-size:14px;\"> <a style=\"color:#3399CC; text-decoration:none;\" href=\"http://www.afyaarabia.com/\" target=\"_blank\">www.afyaarabia.com</a> </p></td>");
        buffer.append("</tr>\n" +
                "                    <tr>\n" +
                "                        <td><p style=\"margin:0px; text-align:center; font-family:Arial, Helvetica, sans-serif; font-size:14px;\"> <a style=\"color:#3399CC; text-decoration:none;\" href=\"#\">Customer Support</a> | <a style=\"color:#3399CC; text-decoration:none;\" href=\"#\">Download App</a> | <a style=\"color:#3399CC; text-decoration:none;\" href=\"#\">Feedback</a> | <a style=\"color:#3399CC; text-decoration:none;\" href=\"#\">Contact Us </a></p> </td>\n" +
                "                    </tr>");
        buffer.append("<tr>\n" +
                "                        <td>&nbsp;</td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td>\n" +
                "                            <p style=\"font-family:Arial, Helvetica, sans-serif; font-size:12px; line-height:20px; text-align:center;\"><strong>&copy; 2015, afyaarabia.com &ndash; All Rights  Reserved</strong><br />");
        buffer.append("HQ: Alqiblla, Ali Al-Salem Street, Al-Fares Complex. Kuwait City, P.O Box 29897 Safat, 13159 Kuwait</p>\n" +
                "                            <p style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; line-height:16px; color:#999999;\">Disclaimer:\n" +
                "                                This notification is not designed to and does not provide medical advice, professional diagnosis, opinion, treatment but is a mere facilitator for medical related services to you. Through this notification and linkages to other sites, Afyaarabia provides general information for facilitation of services requested only. Afyaarabia is not liable or responsible for any advice, course of treatment, diagnosis or any other information, services or product you obtain through this notification.</p>\n" +
                "                            <p style=\"font-family:Arial, Helvetica, sans-serif; font-size:11px; line-height:16px; color:#999999;\">&nbsp;</p></td>");
        buffer.append("</tr><table></td></tr>" +
                "                </table>          </td>" +
                "            </tr>" +
                "        </table></td>");
        buffer.append("</tr>" +
                "</table>" +
                "" +
                "</body>" +
                "</html>");
        /*buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:3px;\">Respected <strong><u>"+user.getPerson().getFirstName() + " " +user.getPerson().getLastName() + "</u></strong>,<br/><br/>Welcome to Afya ecosystem. Your user-name is " + user.getUsername() + ".<br/><br/>" +
                "Password is " + user.getPassword() + ". You are requested to change password during your first log in.<br/><br/>" +
                "You can login to Afya link: <a style=\"color:#3399CC; text-decoration:none;\" href=\"https://www.afyaarabia.com/afya-portal/login\">https://www.afyaarabia.com/afya-portal/login</a><br/><br/>" +
                "Best Regards,<br/>Afya Community Care</p>");
        buffer.append("</body>");
        buffer.append("</html>");*/
        return buffer.toString();
    }
    public static String getSubjectOfRegistrationConfirmationMail() {
        return "Welcome to Afya Ecosystem::Employee Registration";
    }
    public static String getBodyForPatientRegistration(PatientViewObject patientVO) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        buffer.append("<head>");
        buffer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />");
        /*buffer.append("<title>Afya - Patient Creation Notification Email</title>");*/
        buffer.append("</head>");
        buffer.append("<body style=\"margin:0px; padding:0px;\">");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:3px;\">Respected <strong><u>"+patientVO.getPatient().getFirstName() + " " +patientVO.getPatient().getLastName() + "</u></strong>,<br/><br/>Welcome to Afya ecosystem.<br/><br/>" +
                "You are requested to change password during your first log in.<br/><br/>" +
                "You can login to Afya link: <a style=\"color:#3399CC; text-decoration:none;\" href=\"https://www.afyaarabia.com/afya-portal/login\">https://www.afyaarabia.com/afya-portal/login</a><br/><br/>" +
                "Best Regards,<br/>Afya Community Care</p>");
        buffer.append("</body>");
        buffer.append("</html>");
        return buffer.toString();
    }
    public static String getSubjectOfPatientRegistrationConfirmationMail() {
        return "Welcome to Afya Ecosystem::Patient Registration";
    }
}
