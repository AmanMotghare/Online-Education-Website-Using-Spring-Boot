package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.example.demo.model.Course;
import com.example.demo.model.CourseEnrolled;
import com.example.demo.model.CourseTopic;
import com.example.demo.model.Student;
import com.example.demo.repository.CourseEnrollRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.CourseTopicRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.AuthenticateService;



@Controller
public class StudentController {
	
	@Autowired
	StudentRepository studRepo;
	
	CourseEnrolled result;
	
	@RequestMapping("/redirecturl")
	public RedirectView redirecttourl() {
		RedirectView redirectView = new RedirectView();
		redirectView.setUrl("/allCoursesHomepage");
		return redirectView;
	}
	
	
	/*** Open Student Registration Page ***/
	@RequestMapping("/studentReg")
	String studentRegistrationPage(Model model) {
		
		Student student =new Student();
		model.addAttribute("student",student);
		
		return "studentRegistration";
		
	}
	
	/*** Add Student Data ***/
	@RequestMapping("/addStudentData")
	String addStudentData(@ModelAttribute("student") Student student) {
		
		studRepo.save(student);
		System.out.println("Data added to DataBase.");
		return "redirect:/allStudents";
	}
	
	/*** View Student Data ***/
	@RequestMapping("/allStudents")
	String showAllStudents(Model model){
		
		List<Student> list = studRepo.findAll();
		
		model.addAttribute("students",list);
		
		return "allStudents";
	}
	
	/*** Delete Student Data ***/
	@RequestMapping("/deleteStudentData/{id}")
	String deleteStudent(@PathVariable ("id") int id) {
		
		studRepo.deleteById(id);
		return "redirect:/allStudents";
	}
	
	/*** Update Student Data ***/
	@RequestMapping("/updateStudentData/{id}")
	String updateStudent(@PathVariable("id") int id, Model model) {
		
		Student student = studRepo.getReferenceById(id);
		
		model.addAttribute("students",student);
		
		return"updateStudentForm";
	}
	
	/*** Login Student ***/
	@RequestMapping("login-student")
	String openStudentLogin() {
		return "studentLoginPage";
	}
	
	/*** STUDENT-DASHBOARD ***/
	@Autowired
	CourseRepository courseRepo ;
	@RequestMapping("studentdashboard")
	String openStudentDashboard(Model model, HttpSession session) {
		
		String studentEmail = (String) session.getAttribute("sessionStudent") ;
		
		if( studentEmail != null) {
			
			//Fetching no. of courses enrolled by students
			Student student = studRepo.findByEmail(studentEmail);
			model.addAttribute("student",student);
			
			System.out.println("Enrolled courses upper : " + student.getCoursesEnrolledNo());
			
			//Fetching all published courses for students
			List<Course> list = courseRepo.findByStatus("Published");
			model.addAttribute("allCoursesList",list);
			
			return "studentdashboard";
		}
		else {
			return "redirect:/login-student";
		}
		
		
	}
	
	
	@Autowired
	AuthenticateService authserv;
	
	@RequestMapping("/loginStudent")
	String loginAuthenticateStudent(@RequestParam("email") String email,
			@RequestParam("password") String password,
			HttpSession session, Model model) {
		
		if(authserv.studentAuthenticate(email, password)) {
			session.setAttribute("sessionStudent", email);
			return "redirect:/studentdashboard";
		}
		else {
			System.out.println("Login Failed !");
			session.setAttribute("errMsg", "Invalid Credentials !!");
			return "redirect:/login-student";
		}
	}
	
	@RequestMapping("/logout-student")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login-student";
    }
	
	/*** STUDENT- Enroll to Course ***/
	
	@Autowired
	CourseTopicRepository topicRepo;
	@Autowired
	CourseEnrollRepository enrollRepo;
	
	@RequestMapping("/enroll/{title}/{student}")
	String enrollCourse(@PathVariable("title") String title,
			@PathVariable("student") String student, Model model) {
		
		CourseEnrolled result = enrollRepo.findByStudentEmailAndEnrolledCourse(student, title);
		
		if(result == null) {
			//Saving details in Enrolled courses table.
			CourseEnrolled enroll = new CourseEnrolled(student, title);
			enrollRepo.save(enroll);
			
			//Fetching all the courses enrolled by the student.
			List<CourseEnrolled> studentList =  enrollRepo.findByStudentEmail(student);
			
			//Fetching details of the student.
			Student stud = studRepo.findByEmail(student);
			System.out.println("Student data : "+ stud);
			
			//setting the data in model Student
			System.out.println("No. of Courses Enrolled by Student : "+ studentList.size());
			stud.setCoursesEnrolledNo(studentList.size());
			
			//updating the data
			studRepo.save(stud);
			
			//Fetching topics to show on the courseSingle page.
			List<CourseTopic> list =  topicRepo.findBycourseTitle(title);
			model.addAttribute("courseTopics",list);
			
			//Fetching author details to show on the courseSingle page.
			Course course = courseRepo.findBycourseTitle(title);
			
			model.addAttribute("coursename", course.getCourseTitle());
			model.addAttribute("authoremail", course.getAuthorEmail());
			
			return "/courseSingle";
		}
		
		else {
			model.addAttribute("alreadyEnrolled", true);
			return "redirect:/studentdashboard";
		}	
		
	}
	
	
	/**Student Enrolled Courses**/
	
	@RequestMapping("/enrolledCourses/{student}")
	String enrolledCourses(@PathVariable("student") String studentEmail, 
			HttpSession session, Model model) {
		
		if(session.getAttribute("sessionStudent") != null) {
			
			List<CourseEnrolled> courses = enrollRepo.findByStudentEmail(studentEmail);
			model.addAttribute("courses",courses);
			
			//Fetching no. of courses enrolled by students
			Student student = studRepo.findByEmail(studentEmail);
			model.addAttribute("student",student);
			
			
			return "enrolledCourses";
		}
		else {
			return "redirect:/login-student";
		}
		
	}
	
	
}
