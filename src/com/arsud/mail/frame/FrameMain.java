package com.arsud.mail.frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.arsud.mail.utils.MailAssistance;

@SuppressWarnings("serial")
public class FrameMain extends JFrame {

	// UI
	private JPanel contentPane, titlePanel, mainPanel, bottomPanel;
	private JLabel lblTitle, lblFrom, lblTo, lblContents, lblCC;
	private JTextField tfTitle, tfFrom, tfTo, tfCC;
	private JTextArea taContents;
	private JButton btnSend, btnAddFile;
	private JList<String> listFile;
	private HashSet<String> fileSet;
	private JScrollPane scrp;

	private MailAssistance mailAsst;

	public FrameMain() {
		initComponent();
		setEvt();
		setBounds(100, 100, 1000, 562);
		setTitle("메일 전송 프로그램");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		mailAsst = new MailAssistance();
		tfFrom.setText(mailAsst.getFromMail());
		taContents.setText(mailAsst.getDefaultMessage());
	}

	private void setEvt() {
		btnSend.addActionListener(e -> {
			if (fileSet != null) {
				fileSet.stream().forEach(fs -> mailAsst.addFile(fs));
			}
			mailAsst.sendMail(new ArrayList<String>(Arrays.asList(tfTo.getText().split(";"))),
					new ArrayList<String>(Arrays.asList(tfCC.getText().split(";"))), tfTitle.getText(),
					taContents.getText());
			JOptionPane.showMessageDialog(null, "메일 전송 완료~!");
			resetComponent();
		});

		btnAddFile.addActionListener(e -> {
			FileDialog dlg = new FileDialog(this, "파일 선택", FileDialog.LOAD);
			dlg.setVisible(true);

			if (fileSet == null) {
				fileSet = new HashSet<String>();
			}
			if (dlg.getFile() != null) {
				fileSet.add(dlg.getDirectory() + dlg.getFile());
				listFile.setListData(fileSet.stream().toArray(String[]::new));
			}
		});

		listFile.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				if (e.getKeyCode() == 127) {
					if (listFile.getSelectedIndex() != -1) {
						fileSet.remove(listFile.getSelectedValue());
						listFile.setListData(fileSet.stream().toArray(String[]::new));
					}
				}
			}
		});
	}

	private void initComponent() {
		contentPane = new JPanel();
		setContentPane(contentPane);

		titlePanel = new JPanel();
		mainPanel = new JPanel();
		bottomPanel = new JPanel();

		contentPane.setLayout(new BorderLayout());
		contentPane.add(titlePanel, BorderLayout.NORTH);
		contentPane.add(mainPanel, BorderLayout.CENTER);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));

		titlePanel.setLayout(new GridLayout(0, 1));
		mainPanel.setLayout(new BorderLayout());

		lblTitle = new JLabel("제목");
		lblFrom = new JLabel("보내는 사람");
		lblTo = new JLabel("받는 사람 (세미콜론으로 구분)");
		lblCC = new JLabel("참조 (세미콜론으로 구분)");
		lblContents = new JLabel("내용");

		tfTitle = new JTextField();
		tfFrom = new JTextField();
		tfTo = new JTextField();
		tfCC = new JTextField();
		listFile = new JList<String>();
		taContents = new JTextArea();
		scrp = new JScrollPane(listFile);

		tfFrom.setEditable(false);

		titlePanel.add(lblTitle);
		titlePanel.add(tfTitle);
		titlePanel.add(lblFrom);
		titlePanel.add(tfFrom);
		titlePanel.add(lblTo);
		titlePanel.add(tfTo);
		titlePanel.add(lblCC);
		titlePanel.add(tfCC);
		titlePanel.add(lblContents);

		mainPanel.add(scrp, BorderLayout.NORTH);
		mainPanel.add(taContents, BorderLayout.CENTER);

		btnSend = new JButton("전송");
		btnAddFile = new JButton("파일 첨부");

		bottomPanel.add(btnAddFile);
		bottomPanel.add(btnSend);

		scrp.setPreferredSize(new Dimension(scrp.getWidth(), 50));
	}

	private void resetComponent() {
		tfCC.setText("");
		tfTo.setText("");
		tfTitle.setText("");
		if (fileSet == null)
			fileSet = new HashSet<String>();
		fileSet.clear();
		listFile.setListData(fileSet.stream().toArray(String[]::new));
		taContents.setText(mailAsst.getDefaultMessage());
	}

}
