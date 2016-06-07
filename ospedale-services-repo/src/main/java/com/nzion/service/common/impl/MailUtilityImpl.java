package com.nzion.service.common.impl;

import com.nzion.service.common.MailUtility;

/**
 * Created by Nthdimenzion on 5/29/2015.
 */
public class MailUtilityImpl implements MailUtility {
    @Override
    public String constructBodyOfEmail() {
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
        buffer.append("<p style=\"margin:20px; font-family:Arial, Helvetica, sans-serif; font-size:22px; margin-top:5px; \">Saturday May 23, 2015 at  05:30PM </p>          </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td colspan=\"3\">");
        buffer.append("<table width=\"600\" border=\"0\" cellpadding=\"5\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px;\">Dear Krishna,</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Your appointment has been confirmed by the doctor. <br />");
        buffer.append("Following are the details for your reference</p>");
        buffer.append("<table width=\"500\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\" align=\"center\">");
        buffer.append("<tr>");
        buffer.append("<td width=\"120\" style=\"border-bottom:solid 1px #CCCCCC; border-top:solid 5px #D1D1D1; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Doctor's Name: </strong></p>                </td>");
        buffer.append("<td width=\"334\" style=\"border-bottom:solid 1px #CCCCCC; border-top:solid 5px #D1D1D1; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Dr. Khaleed Bin Al-Rashid </p>                </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Clinic</strong></p>                </td>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Al-Safat Medical Services </p>                </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong> Where </strong></p></td>");
        buffer.append("<td style=\"border-bottom:solid 1px #CCCCCC; background-color:#F0F0F0;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Sabah Al Salem Street<br />");
        buffer.append("Kuwait<br />");
        buffer.append("Ph: +965 2227 5900<br />");
        buffer.append("Email: appointments@alsafatmedical.com</p>                </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"border-bottom:solid 5px #D1D1D1;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong> Reason </strong></p> </td>");
        buffer.append("<td style=\"border-bottom:solid 5px #D1D1D1;\"><p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"> Teeth Cleaning </p> </td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">&nbsp;</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Kind Regards, </p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\"><strong>Customer Care Team</strong><br />");
        buffer.append("Al-Safat Medical Services</p>");
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:14px; margin:3px; margin-bottom:9px; line-height:24px;\">Contact Al-Safat Medical Services on");
        buffer.append("Ph:");
        buffer.append("+965 2227 5900 or <br />");
        buffer.append("email: appointments@alsafatmedical.com             </p>\t</td>");
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
        buffer.append("<p style=\"font-family:Arial, Helvetica, sans-serif; font-size:12px; line-height:20px; text-align:center;\"><strong>&copy; 2015, afyaarabia.com &ndash; All Rights  Reserved</strong><br />");
        buffer.append("HQ: Alqiblla, Ali Al-Salem Street, Al-Fares Complex. Kuwait City, P.O Box 29897 Safat, 13159 Kuwait</p>");
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
}
