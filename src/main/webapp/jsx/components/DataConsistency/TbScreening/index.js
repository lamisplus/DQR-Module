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


const TbScreening = (props) => {
    const classes = useStyles();
    const [tbScreening, setTbScreening] = useState({});
    const [facilities, setFacilities] = useState([]);
    const [showPatientDetail, setPatientDetail] = useState(false);
    const [getHeaderInfo, setGetHeaderInfo] = useState("");
    const [tbPatientsView, setTbPatientsView] = useState([])



     useEffect(() => {
      Facilities();
      loadTbScreening();
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

  const loadTbScreening = () => {
    axios
      .get(`${baseUrl}dqr/tb-summary`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setTbScreening(response.data);
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
            setTbPatientsView(response.data);
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
                    <h3>TB Screening</h3>
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
                                    Proportion of all active patients enrolled in ART with a documented TB screening
                                </td>
                                <td>{tbScreening[0]?.tbScreenNumerator}</td>
                                <td>{tbScreening[0]?.tbScreenDenominator}</td>
                                <td>{tbScreening[0]?.tbScreenPerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with documented month of ARV refill", "" )}> View</p>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    2
                                </th>
                                <td>
                                    Proportion of all active patients screened for TB with documented TB screening outcome
                                </td>
                                <td>{tbScreening[0]?.docAndCompletedNumerator}</td>
                                <td>{tbScreening[0]?.docAndCompletedDenominator}</td>
                                <td>{tbScreening[0]?.docAndCompletedPerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with ART Start Date", "" )}> View</p>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    3
                                </th>
                                <td>
                                    Proportion of all active patients with documented TB status as at last visit
                                </td>
                                <td>{tbScreening[0]?.tbstatusNumerator}</td>
                                <td>{tbScreening[0]?.tbstatusDenominator}</td>
                                <td>{tbScreening[0]?.tbstatusPerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with First HIV confirmed test Date", "" )}> View</p>
                                </div>            
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    4
                                </th>
                                <td>
                                Proportion of all presumptive TB patients who have documented date of sample collection
                                </td>
                                <td>{tbScreening[0]?.preSampleNumerator}</td>
                                <td>{tbScreening[0]?.preSampleDenominator}</td>
                                <td>{tbScreening[0]?.preSamplePerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with documented drug pickup date", "" )}> View</p>
                                </div>  
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    5
                                </th>
                                <td>
                                    Proportion of presumptive TB patients whose samples were collected with documented samples test type
                                </td>
                               <td>{tbScreening[0]?.preSampleTypeNumerator}</td>
                               <td>{tbScreening[0]?.preSampleTypeDenominator}</td>
                               <td>{tbScreening[0]?.preSampleTypePerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with Age at ART Initiation", "" )}> View</p>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    6
                                </th>
                                <td>
                                    Proportion of TB patients with documented TB treatment outcome
                                </td>
                                <td>{tbScreening[0]?.ipt6monthComplNumerator}</td>
                                <td>{tbScreening[0]?.ipt6monthComplDenominator}</td>
                                <td>{tbScreening[0]?.ipt6monthComplPerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of all active patients with Last Drug Regimen", "" )}> View</p>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    7
                                </th>
                                <td>
                                    Proportion of patients on TPT with documented TPT start date
                                </td>
                                <td>{tbScreening[0]?.ipt6monthComplNumerator}</td>
                                <td>{tbScreening[0]?.ipt6monthComplDenominator}</td>
                                <td>{tbScreening[0]?.ipt6monthComplPerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of patients on TPT with documented TPT start date", "" )}> View</p>
                                </div>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    8
                                </th>
                                <td>
                                    Proportion of patients on TPT with documented TPT completion date
                                </td>
                                <td>{tbScreening[0]?.tptstartNumerator}</td>
                                <td>{tbScreening[0]?.tptstartDenominator}</td>
                                <td>{tbScreening[0]?.tptstartPerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of patients on TPT with documented TPT completion date", "" )}> View</p>
                                </div>
                                </td>
                            </tr>
                            <tr>
                       <th scope="row">
                           9
                       </th>
                               <td>
                                    Proportion of patients on TPT with documented TPT completion status
                               </td>
                               <td>{tbScreening[0]?.iptComplStatususNumerator}</td>
                                <td>{tbScreening[0]?.iptComplStatususDenominator}</td>
                                <td>{tbScreening[0]?.iptComplStatususPerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of patients on TPT with documented TPT completion status", "" )}> View</p>
                                </div>
                                </td>
                        </tr>
                        <tr>
                                <th scope="row">
                                    10
                                </th>
                                <td>
                                    Proportion of patients on TPT with documented TPT type
                                </td>
                                <td>{tbScreening[0]?.iptTypeStatusNumerator}</td>
                                <td>{tbScreening[0]?.iptTypeStatusDenominator}</td>
                                <td>{tbScreening[0]?.iptTypeStatusPerformance} %</td>
                                <td>
                                <div>
                                    <p style={{cursor:"pointer" }} onClick={() => viewDetail("Proportion of patients on TPT with documented TPT type", "" )}> View</p>
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
                            data={ tbPatientsView.map((row) => ({
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

export default TbScreening