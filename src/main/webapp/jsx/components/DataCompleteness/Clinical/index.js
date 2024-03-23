import React, { useEffect, useState } from "react";
import axios from "axios";
import MaterialTable from 'material-table';
import { token, url as baseUrl } from "../../../../api";
import { Form, Table } from "reactstrap";
import { makeStyles } from "@material-ui/core/styles";
import { Card, CardContent } from "@material-ui/core";
import "semantic-ui-css/semantic.min.css";
import { Dropdown, Button, Menu, Icon } from "semantic-ui-react";

import ErrorIcon from "@mui/icons-material/Error";
import { FiUploadCloud } from "react-icons/fi";
import { forwardRef } from 'react';
import "react-toastify/dist/ReactToastify.css";
import "react-widgets/dist/css/react-widgets.css";
import CloudUpload from '@material-ui/icons/CloudUpload';
import moment from "moment";
import AddBox from '@material-ui/icons/AddBox';
import ArrowUpward from '@material-ui/icons/ArrowUpward';
import Check from '@material-ui/icons/Check';
import ChevronLeft from '@material-ui/icons/ChevronLeft';
import ChevronRight from '@material-ui/icons/ChevronRight';
import Clear from '@material-ui/icons/Clear';
import DeleteOutline from '@material-ui/icons/DeleteOutline';
import Edit from '@material-ui/icons/Edit';
import FilterList from '@material-ui/icons/FilterList';
import FirstPage from '@material-ui/icons/FirstPage';
import LastPage from '@material-ui/icons/LastPage';
import Remove from '@material-ui/icons/Remove';
import SaveAlt from '@material-ui/icons/SaveAlt';
import Search from '@material-ui/icons/Search';
import ViewColumn from '@material-ui/icons/ViewColumn';

const tableIcons = {
    Add: forwardRef((props, ref) => <AddBox {...props} ref={ref} />),
    Check: forwardRef((props, ref) => <Check {...props} ref={ref} />),
    Clear: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
    Delete: forwardRef((props, ref) => <DeleteOutline {...props} ref={ref} />),
    DetailPanel: forwardRef((props, ref) => <ChevronRight {...props} ref={ref} />),
    Edit: forwardRef((props, ref) => <Edit {...props} ref={ref} />),
    Export: forwardRef((props, ref) => <SaveAlt {...props} ref={ref} />),
    Filter: forwardRef((props, ref) => <FilterList {...props} ref={ref} />),
    FirstPage: forwardRef((props, ref) => <FirstPage {...props} ref={ref} />),
    LastPage: forwardRef((props, ref) => <LastPage {...props} ref={ref} />),
    NextPage: forwardRef((props, ref) => <ChevronRight {...props} ref={ref} />),
    PreviousPage: forwardRef((props, ref) => <ChevronLeft {...props} ref={ref} />),
    ResetSearch: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
    Search: forwardRef((props, ref) => <Search {...props} ref={ref} />),
    SortArrow: forwardRef((props, ref) => <ArrowUpward {...props} ref={ref} />),
    ThirdStateCheck: forwardRef((props, ref) => <Remove {...props} ref={ref} />),
    ViewColumn: forwardRef((props, ref) => <ViewColumn {...props} ref={ref} />)
    };


const useStyles = makeStyles((theme) => ({
    card: {
        margin: theme.spacing(20),
        display: "flex",
        flexDirection: "column",
        alignItems: "center"
    },
    form: {
        width: "100%", // Fix IE 11 issue.
        marginTop: theme.spacing(3),
    },
    submit: {
        margin: theme.spacing(3, 0, 2),
    },
    cardBottom: {
        marginBottom: 20,
    },
    Select: {
        height: 45,
        width: 300,
    },
    button: {
        margin: theme.spacing(1),
    },
    root: {
        '& > *': {
            margin: theme.spacing(1)
        },
        "& .card-title":{
            color:'#fff',
            fontWeight:'bold'
        },
        "& .form-control":{
            borderRadius:'0.25rem',
            height:'41px'
        },
        "& .card-header:first-child": {
            borderRadius: "calc(0.25rem - 1px) calc(0.25rem - 1px) 0 0"
        },
        "& .dropdown-toggle::after": {
            display: " block !important"
        },
        "& select":{
            "-webkit-appearance": "listbox !important"
        },
        "& p":{
            color:'red'
        },
        "& label":{
            fontSize:'14px',
            color:'#014d88',
            fontWeight:'bold'
        }
    },
    demo: {
        backgroundColor: theme.palette.background.default,
    },
    inline: {
        display: "inline",
    },
    error:{
        color: '#f85032',
        fontSize: '12.8px'
    },
    success: {
        color: "#4BB543 ",
        fontSize: "11px",
    },
}));


const Clinical = (props) => {
    const classes = useStyles();
    const [clinical, setClinicals] = useState({});
    const [facilities, setFacilities] = useState([]);
    const [showPatientDetail, setPatientDetail] = useState(false);
    const [getHeaderInfo, setGetHeaderInfo] = useState("");
    const [clinicPatientsView, setClinicPatientsView] = useState([])



     useEffect(() => {
      Facilities();
      loadClinicals();
    }, []);
    const Facilities = () => {
    axios
      .get(`${baseUrl}account`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setFacilities(response.data.currentOrganisationUnitId);
      })
      .catch((error) => {
        console.log(error);
      });
  };
//   const Facilities = () => {
//     axios
//       .get(`${baseUrl}account`, {
//         headers: { Authorization: `Bearer ${token}` },
//       })
//       .then((response) => {
//         setFacilities(response.data.currentOrganisationUnitId);
//       })
//       .catch((error) => {
//         console.log(error);
//       });
//   };

  const loadClinicals = () => {
    axios
      .get(`${baseUrl}dqr/clinical-variable-summary?facilityId=${facilities}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setClinicals(response.data);
        console.log(response.data)
      })
      .catch((error) => {
        console.log(error);
      });
  };

  const viewDetail =(headerTitle,patientDemoObj)=>{
    setPatientDetail(true)
    setGetHeaderInfo(headerTitle)
    const clinicDemo =patientDemoObj
    axios
          .get(`${baseUrl}dqr/patient-clinic?indicator=${clinicDemo}`, {
            headers: { Authorization: `Bearer ${token}` },
          })
          .then((response) => {
            setClinicPatientsView(response.data);
            //console.log(response.data[0])
          })
          .catch((error) => {
            console.log(error);
          });
    }
    const BackToList=()=> {
      setPatientDetail(false)
    }

    return (
        <>
           
            <Card className={classes.root}>
                <CardContent>
                    <h3>Clinical Variables</h3>
                    <div className="col-xl-12 col-lg-12">
                    {!showPatientDetail &&(<>
                        <Table bordered>
                            <thead>
                            <tr>
                                <th>
                                    #
                                </th>
                                <th>
                                    Complete Variables
                                </th>
                                <th>
                                    Numerator
                                </th>
                                <th>
                                  Denominator
                                </th>
                                <th>
                                  Variance
                                </th>
                                <th>
                                  Performance
                                </th>
                                <th>
                                    Action
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <th scope="row">
                                    1
                                </th>
                                <td>
                                    Proportion of all active patients with documented month of ARV refill
                                </td>
                                <td>{clinical[0]?.refillMonthNumerator}</td>
                                <td>{clinical[0]?.refillMonthDenominator}</td>
                                <td> </td>
                                <td style={{ backgroundColor: clinical[0]?.refillMonthPerformance >= 95 ? 'green' : clinical[0]?.refillMonthPerformance >= 90 ? 'yellow' : 'red', color: clinical[0]?.refillMonthPerformance >= 95 ? 'white' : clinical[0]?.refillMonthPerformance >= 90 ? 'black' : 'white',
                                  textAlign: 'center', fontWeight: 'bold' }}>
                                 {clinical[0]?.refillMonthPerformance} %
                                </td>

                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of all active patients with documented month of ARV refill", "clinic12" )}
                                    >View</Button>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    2
                                </th>
                                <td>
                                    Proportion of all active patients with ART Start Date
                                </td>
                                <td>{clinical[0]?.startDateNumerator}</td>
                                <td>{clinical[0]?.startDateDenominator}</td>
                                <td> </td>
                                <td style={{ backgroundColor: clinical[0]?.startDatePerformance >= 95 ? 'green' : clinical[0]?.startDatePerformance >= 90 ? 'yellow' : 'red', color: clinical[0]?.startDatePerformance >= 95 ? 'white' : clinical[0]?.startDatePerformance >= 90 ? 'black' : 'white',
                                  textAlign: 'center', fontWeight: 'bold' }}>
                                    {clinical[0]?.startDatePerformance} %
                                 </td>

                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of all active patients with ART Start Date", "clinic0" )}
                                    >View</Button>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    3
                                </th>
                                <td>
                                    Proportion of all active patients with First HIV confirmed test Date
                                </td>
                                <td>{clinical[0]?.confirmDateNumerator}</td>
                                <td>{clinical[0]?.confirmDateDenominator}</td>
                                <td> </td>
                                <td style={{ backgroundColor: clinical[0]?.confirmDatePerformance >= 95 ? 'green' : clinical[0]?.confirmDatePerformance >= 90 ? 'yellow' : 'red', color: clinical[0]?.confirmDatePerformance >= 95 ? 'white' : clinical[0]?.confirmDatePerformance >= 90 ? 'black' : 'white',
                                  textAlign: 'center', fontWeight: 'bold' }}>
                                    {clinical[0]?.confirmDatePerformance} %
                                </td>

                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of all active patients with First HIV confirmed test Date", "clinic2" )}
                                    >View</Button>
                                </div>            
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    4
                                </th>
                                <td>
                                    Proportion of all active patients with documented drug pickup date
                                </td>
                                <td>{clinical[0]?.lastPickNumerator}</td>
                                <td>{clinical[0]?.lastPickDenominator}</td>
                                 <td> </td>
                                 <td style={{ backgroundColor: clinical[0]?.lastPickPerformance >= 95 ? 'green' : clinical[0]?.lastPickPerformance >= 90 ? 'yellow' : 'red', color: clinical[0]?.lastPickPerformance >= 95 ? 'white' : clinical[0]?.lastPickPerformance >= 90 ? 'black' : 'white',
                                  textAlign: 'center', fontWeight: 'bold' }}>
                                    {clinical[0]?.lastPickPerformance} %
                                  </td>

                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of all active patients with documented drug pickup date", "clinic6" )}
                                    >View</Button>
                                </div>  
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    5
                                </th>
                                <td>
                                    Proportion of all active patients with Age at ART Initiation
                                </td>
                               <td>{clinical[0]?.ageNumerator}</td>
                               <td>{clinical[0]?.ageDenominator}</td>
                                <td> </td>
                                <td style={{ backgroundColor: clinical[0]?.agePerformance >= 95 ? 'green' : clinical[0]?.agePerformance >= 90 ? 'yellow' : 'red', color: clinical[0]?.agePerformance >= 95 ? 'white' :clinical[0]?.agePerformance >= 90 ? 'black' : 'white',
                                  textAlign: 'center', fontWeight: 'bold' }}>
                                    {clinical[0]?.agePerformance} %
                                 </td>
                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of all active patients with Age at ART Initiation", "clinic3" )}
                                    >View</Button>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    6
                                </th>
                                <td>
                                    Proportion of all active patients with Last Drug Regimen
                                </td>
                                <td>{clinical[0]?.regimenNumerator}</td>
                                <td>{clinical[0]?.regimenDenominator}</td>
                                 <td> </td>
                                 <td style={{ backgroundColor: clinical[0]?.regimenPerformance >= 95 ? 'green' : clinical[0]?.regimenPerformance >= 90 ? 'yellow' : 'red', color: clinical[0]?.refillMonthPerformance >= 95 ? 'white' : clinical[0]?.refillMonthPerformance >= 90 ? 'black' : 'white',
                                  textAlign: 'center', fontWeight: 'bold' }}>
                                    {clinical[0]?.regimenPerformance} %
                                </td>

                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of all active patients with Last Drug Regimen", "clinic6" )}
                                    >View</Button>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    7
                                </th>
                                <td>
                                    Proportion of all active patients with documented target group
                                </td>
                                <td>{clinical[0]?.targNumerator}</td>
                                <td>{clinical[0]?.targDenominator}</td>
                                 <td> </td>
                                 <td style={{ backgroundColor: clinical[0]?.targPerformance >= 95 ? 'green' : clinical[0]?.targPerformance >= 90 ? 'yellow' : 'red', color: clinical[0]?.targPerformance>= 95 ? 'white' : clinical[0]?.targPerformance>= 90 ? 'black' : 'white',
                                  textAlign: 'center', fontWeight: 'bold' }}>
                                 {clinical[0]?.targPerformance} % </td>

                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of all active patients with documented target group", "clinic4" )}
                                    >View</Button>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    8
                                </th>
                                <td>
                                    Proportion of all active patients with a documented care entry point
                                </td>
                                <td>{clinical[0]?.entryNumerator}</td>
                                <td>{clinical[0]?.entryDenominator}</td>
                                <td> </td>
                                <td style={{ backgroundColor: clinical[0]?.entryPerformance >= 95 ? 'green' : clinical[0]?.entryPerformance >= 90 ? 'yellow' : 'red', color: clinical[0]?.entryPerformance >= 95 ? 'white' : clinical[0]?.entryPerformance >= 90 ? 'black' : 'white',
                                  textAlign: 'center', fontWeight: 'bold' }}>
                                    {clinical[0]?.entryPerformance} % </td>

                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of all active patients with a documented care entry point", "clinic5" )}
                                    >View</Button>
                                </div>
                                </td>
                            </tr>
                            {/* <tr>
                       <th scope="row">
                           9
                       </th>
                               <td>
                               Proportion of all active patients with Last Drug Regimen Code
                               </td>
                               <td></td>
                               <td></td>
                               <td> %</td>
                               <td>
                               <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with Last Drug Regimen Code", "" )}> View</p>
                                </div>

                               </td>
                        </tr> */}
                        <tr>
                                <th scope="row">
                                    10
                                </th>
                                <td>
                                    Proportion of all active patients that had documented last Clinic visit date
                                </td>
                                <td>{clinical[0]?.lastVisitNumerator}</td>
                                <td>{clinical[0]?.lastVisitDenominator}</td>
                                 <td> </td>
                                  <td style={{
                                      backgroundColor: clinical[0]?.lastVisitPerformance >= 95 ? 'green' : clinical[0]?.lastVisitPerformance >= 90 ? 'yellow' : 'red', color: clinical[0]?.lastVisitPerformance >= 95 ? 'white' : clinical[0]?.lastVisitPerformance >= 90 ? 'black' : 'white',
                                      textAlign: 'center', fontWeight: 'bold' }}>
                                      {clinical[0]?.lastVisitPerformance} %
                                  </td>
                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail(" Proportion of all active patients that had documented last Clinic visit date", "clinic6" )}
                                    >View</Button>
                                </div>

                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    11
                                </th>
                                <td>
                                    Proportion of all active patients with documented weight
                                </td>
                                <td>{clinical[0]?.weightNumerator}</td>
                                <td>{clinical[0]?.weightDenominator}</td>
                                 <td> </td>
                                 <td style={{ backgroundColor: clinical[0]?.weightPerformance >= 95 ? 'green' : clinical[0]?.weightPerformance >= 90 ? 'yellow' : 'red', color: clinical[0]?.weightPerformance >= 95 ? 'white' : clinical[0]?.weightPerformance >= 90 ? 'black' : 'white',
                                  textAlign: 'center', fontWeight: 'bold' }}>
                                  {clinical[0]?.weightPerformance} % </td>
                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of all active patients with documented weight", "clinic7" )}
                                    >View</Button>
                                </div>

                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    12
                                </th>
                                <td>
                                    Proportion of all active female {">"}12 age with Pregnancy status at last visit
                                </td>
                               <td>{clinical[0]?.pregNumerator}</td>
                               <td>{clinical[0]?.pregDenominator}</td>
                               <td> </td>
                               <td style={{ backgroundColor: clinical[0]?.pregPerformance >= 95 ? 'green' : clinical[0]?.pregPerformance >= 90 ? 'yellow' : 'red', color: clinical[0]?.pregPerformance >= 95 ? 'white' : clinical[0]?.pregPerformance >= 90 ? 'black' : 'white',
                                  textAlign: 'center', fontWeight: 'bold' }}>
                                  {clinical[0]?.pregPerformance} % </td>

                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of all active female > 12 age with Pregnancy status at last visit", "clinic8" )}
                                    >View</Button>
                                </div>

                                </td>
                             </tr>
                             <tr>
                                <th scope="row">
                                    13
                                </th>
                                <td>
                                    Proportion of all active patients with documented dates of HIV diagnosis
                                </td>
                                <td>{clinical[0]?.diagnoseNumerator}</td>
                                <td>{clinical[0]?.diagnoseDenominator}</td>
                                <td> </td>
                                <td style={{ backgroundColor: clinical[0]?.diagnosePerformance >= 95 ? 'green' : clinical[0]?.diagnosePerformance >= 90 ? 'yellow' : 'red', color: clinical[0]?.diagnosePerformance >= 95 ? 'white' : clinical[0]?.diagnosePerformance >= 90 ? 'black' : 'white',
                                  textAlign: 'center', fontWeight: 'bold' }}>
                                  {clinical[0]?.diagnosePerformance} % </td>

                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of all active patients with documented dates of HIV diagnosis", "clinic9" )}
                                    >View</Button>
                                </div>

                                </td>
                             </tr>
                           <tr>
                                <th scope="row">
                                    14
                                </th>
                                <td>
                                    Proportion of all active patients with documented HIV enrolment date
                                </td>
                                <td>{clinical[0]?.enrolledDateNumerator}</td>
                                <td>{clinical[0]?.enrolledDateDenominator}</td>
                                <td> </td>
                                <td style={{ backgroundColor: clinical[0]?.enrolledDatePerformance >= 95 ? 'green' : clinical[0]?.enrolledDatePerformance >= 90 ? 'yellow' : 'red', color: clinical[0]?.enrolledDatePerformance >= 95 ? 'white' : clinical[0]?.enrolledDatePerformance >= 90 ? 'black' : 'white',
                                  textAlign: 'center', fontWeight: 'bold' }}>
                                  {clinical[0]?.enrolledDatePerformance} % </td>
                                <td>
                                <div>
                                <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of all active patients with documented HIV enrolment date", "clinic10" )}
                                    >View</Button>
                                </div>

                                </td>
                              </tr>
                               <tr>
                               <th scope="row">
                                   15
                               </th>
                               <td>
                                    Proportion of all active patients with documented ART Commencement date
                               </td>
                               <td>{clinical[0]?.commencedNumerator}</td>
                               <td>{clinical[0]?.commencedDenominator}</td>
                               <td> </td>
                               <td style={{ backgroundColor: clinical[0]?.commencedPerformance >= 95 ? 'green' : clinical[0]?.commencedPerformance >= 90 ? 'yellow' : 'red', color: clinical[0]?.commencedPerformance >= 95 ? 'white' : clinical[0]?.commencedPerformance >= 90 ? 'black' : 'white',
                                  textAlign: 'center', fontWeight: 'bold' }}>
                                  {clinical[0]?.commencedPerformance} % </td>
                               <td>
                               <div>
                               <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                                    onClick={() => viewDetail("Proportion of all active patients with documented ART Commencement date", "clinic11" )}
                                    >View</Button>
                                </div>

                               </td>
                             </tr>
                            </tbody>
                        </Table>
                        </>)}
            {showPatientDetail &&(<>
                      <Button
                        variant="contained"
                        style={{backgroundColor:"#014d88", }}
                        className=" float-right mr-1"
                        //startIcon={<FaUserPlus />}
                        onClick={BackToList}
                        >
                        <span style={{ textTransform: "capitalize", color:"#fff" }}> {"<<"} Back </span>
                        </Button>
                        <br/>
                        <br/> 
                        <MaterialTable
                            icons={tableIcons}
                            title={getHeaderInfo}
                            columns={[

                              {
                                title: "Hospital Number",
                                field: "hospitalNumber",
                              },
                              { title: "Sex ", field: "sex", filtering: false },
                              { title: "Date Of Birth", field: "dob", filtering: false },
                              { title: "Status", field: "status", filtering: false },

                            ]}
                            data={ clinicPatientsView.map((row) => ({
                              //Id: manager.id,
                              hospitalNumber: row.hospitalNumber,
                              sex: row.sex,
                              dob: row.dateOfBirth,
                              status:row.status

                            }))}

                            options={{
                              headerStyle: {
                                backgroundColor: "#014d88",
                                color: "#fff",
                              },
                              searchFieldStyle: {
                                width : '200%',
                                margingLeft: '250px',
                              },
                              filtering: false,
                              exportButton: true,
                              searchFieldAlignment: 'left',
                              pageSizeOptions:[10,20,100],
                              pageSize:10,
                              debounceInterval: 400
                            }}
                        />
                        </>)}
                    </div>
                </CardContent>
            </Card>

        </>
    );
};

export default Clinical